const express = require('express');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const Vote = require('../models/Vote');
const User = require('../models/User');
const Candidate = require('../models/Candidate');
const Election = require('../models/Election');
const FaceRecognitionService = require('../services/faceRecognitionService');
const BlockchainService = require('../services/blockchainService');

const router = express.Router();

// JWT Secret
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';

// Middleware to verify JWT token
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Access token required' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ message: 'Invalid or expired token' });
    }
    req.user = user;
    next();
  });
};

// Get active elections
router.get('/elections', authenticateToken, async (req, res) => {
  try {
    const elections = await Election.find({ isActive: true })
      .select('title description startDate endDate constituencies totalVoters totalVotes');

    res.json(elections);
  } catch (error) {
    console.error('Get elections error:', error);
    res.status(500).json({ message: 'Failed to fetch elections' });
  }
});

// Get candidates for an election
router.get('/elections/:electionId/candidates', authenticateToken, async (req, res) => {
  try {
    const { electionId } = req.params;

    const candidates = await Candidate.find({ 
      electionId, 
      isActive: true 
    }).populate('userId', 'firstName lastName');

    res.json(candidates);
  } catch (error) {
    console.error('Get candidates error:', error);
    res.status(500).json({ message: 'Failed to fetch candidates' });
  }
});

// Cast a vote
router.post('/cast-vote', [
  body('electionId').isMongoId(),
  body('candidateId').isMongoId(),
  body('faceData').notEmpty()
], authenticateToken, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { electionId, candidateId, faceData } = req.body;
    const voterId = req.user.userId;

    // Check if user exists and is approved
    const user = await User.findById(voterId);
    if (!user || !user.isApproved) {
      return res.status(403).json({ message: 'User not authorized to vote' });
    }

    // Check if user has already voted in this election
    const existingVote = await Vote.findOne({ voterId, electionId });
    if (existingVote) {
      return res.status(400).json({ message: 'You have already voted in this election' });
    }

    // Verify face data
    const faceVerification = await FaceRecognitionService.verifyFace(faceData);
    if (!faceVerification.isValid) {
      return res.status(400).json({ message: 'Face verification failed' });
    }

    // Get election details
    const election = await Election.findById(electionId);
    if (!election || !election.isActive) {
      return res.status(400).json({ message: 'Election is not active' });
    }

    // Get candidate details
    const candidate = await Candidate.findById(candidateId);
    if (!candidate || !candidate.isActive || candidate.electionId.toString() !== electionId) {
      return res.status(400).json({ message: 'Invalid candidate' });
    }

    // Get previous vote hash for blockchain
    const lastVote = await Vote.findOne({ electionId }).sort({ timestamp: -1 });
    const previousHash = lastVote ? lastVote.hash : '0';

    // Create vote with blockchain hash
    const vote = new Vote({
      voterId,
      candidateId,
      electionId,
      constituency: candidate.constituency,
      ipAddress: req.ip,
      userAgent: req.get('User-Agent'),
      previousHash,
      nonce: 0
    });

    // Generate hash
    vote.hash = vote.generateHash();

    // Verify blockchain integrity
    const blockchainVerification = await BlockchainService.verifyVote(vote);
    if (!blockchainVerification.isValid) {
      return res.status(500).json({ message: 'Blockchain verification failed' });
    }

    await vote.save();

    // Update candidate vote count
    candidate.voteCount += 1;
    await candidate.save();

    // Update election vote count
    election.totalVotes += 1;
    await election.save();

    // Mark user as voted
    user.hasVoted = true;
    await user.save();

    // Send confirmation email
    await EmailService.sendVoteConfirmation(user.email, election.title);

    res.json({
      message: 'Vote cast successfully',
      voteId: vote._id,
      hash: vote.hash
    });

  } catch (error) {
    console.error('Cast vote error:', error);
    res.status(500).json({ message: 'Failed to cast vote' });
  }
});

// Get voting results (only after election ends)
router.get('/results/:electionId', authenticateToken, async (req, res) => {
  try {
    const { electionId } = req.params;

    const election = await Election.findById(electionId);
    if (!election) {
      return res.status(404).json({ message: 'Election not found' });
    }

    // Check if election has ended
    if (new Date() < election.endDate) {
      return res.status(403).json({ message: 'Election is still ongoing' });
    }

    const candidates = await Candidate.find({ electionId })
      .populate('userId', 'firstName lastName')
      .select('partyName partySymbol constituency voteCount photo')
      .sort({ voteCount: -1 });

    const totalVotes = candidates.reduce((sum, candidate) => sum + candidate.voteCount, 0);

    const results = candidates.map(candidate => ({
      ...candidate.toObject(),
      percentage: totalVotes > 0 ? ((candidate.voteCount / totalVotes) * 100).toFixed(2) : 0
    }));

    res.json({
      election: {
        title: election.title,
        totalVoters: election.totalVoters,
        totalVotes,
        endDate: election.endDate
      },
      results
    });

  } catch (error) {
    console.error('Get results error:', error);
    res.status(500).json({ message: 'Failed to fetch results' });
  }
});

// Get user's voting history
router.get('/history', authenticateToken, async (req, res) => {
  try {
    const voterId = req.user.userId;

    const votes = await Vote.find({ voterId })
      .populate('electionId', 'title startDate endDate')
      .populate('candidateId', 'partyName')
      .select('timestamp constituency hash isVerified')
      .sort({ timestamp: -1 });

    res.json(votes);
  } catch (error) {
    console.error('Get voting history error:', error);
    res.status(500).json({ message: 'Failed to fetch voting history' });
  }
});

// Verify vote hash
router.get('/verify/:voteId', authenticateToken, async (req, res) => {
  try {
    const { voteId } = req.params;

    const vote = await Vote.findById(voteId);
    if (!vote) {
      return res.status(404).json({ message: 'Vote not found' });
    }

    const isValid = vote.verifyHash();
    
    if (isValid && !vote.isVerified) {
      vote.isVerified = true;
      vote.verificationTimestamp = new Date();
      await vote.save();
    }

    res.json({
      voteId: vote._id,
      isValid,
      isVerified: vote.isVerified,
      hash: vote.hash,
      timestamp: vote.timestamp
    });

  } catch (error) {
    console.error('Verify vote error:', error);
    res.status(500).json({ message: 'Failed to verify vote' });
  }
});

module.exports = router;
