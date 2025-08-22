const express = require('express');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const Candidate = require('../models/Candidate');
const User = require('../models/User');
const Election = require('../models/Election');

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

// Get all candidates
router.get('/', async (req, res) => {
  try {
    const { electionId, constituency, active } = req.query;
    
    let query = {};
    if (electionId) query.electionId = electionId;
    if (constituency) query.constituency = constituency;
    if (active !== undefined) query.isActive = active === 'true';

    const candidates = await Candidate.find(query)
      .populate('userId', 'firstName lastName email')
      .populate('electionId', 'title startDate endDate')
      .select('-__v')
      .sort({ createdAt: -1 });

    res.json(candidates);
  } catch (error) {
    console.error('Get candidates error:', error);
    res.status(500).json({ message: 'Failed to fetch candidates' });
  }
});

// Get candidate by ID
router.get('/:candidateId', async (req, res) => {
  try {
    const { candidateId } = req.params;

    const candidate = await Candidate.findById(candidateId)
      .populate('userId', 'firstName lastName email phone')
      .populate('electionId', 'title description startDate endDate');

    if (!candidate) {
      return res.status(404).json({ message: 'Candidate not found' });
    }

    res.json(candidate);
  } catch (error) {
    console.error('Get candidate error:', error);
    res.status(500).json({ message: 'Failed to fetch candidate' });
  }
});

// Get candidates for a specific election
router.get('/election/:electionId', async (req, res) => {
  try {
    const { electionId } = req.params;

    const candidates = await Candidate.find({ 
      electionId, 
      isActive: true 
    })
      .populate('userId', 'firstName lastName')
      .select('partyName partySymbol constituency manifesto photo voteCount')
      .sort({ constituency: 1, voteCount: -1 });

    res.json(candidates);
  } catch (error) {
    console.error('Get election candidates error:', error);
    res.status(500).json({ message: 'Failed to fetch candidates' });
  }
});

// Get candidates by constituency
router.get('/constituency/:constituency', async (req, res) => {
  try {
    const { constituency } = req.params;
    const { electionId } = req.query;

    let query = { constituency, isActive: true };
    if (electionId) query.electionId = electionId;

    const candidates = await Candidate.find(query)
      .populate('userId', 'firstName lastName')
      .populate('electionId', 'title startDate endDate')
      .select('partyName partySymbol manifesto photo voteCount')
      .sort({ voteCount: -1 });

    res.json(candidates);
  } catch (error) {
    console.error('Get constituency candidates error:', error);
    res.status(500).json({ message: 'Failed to fetch candidates' });
  }
});

// Update candidate profile (only by the candidate themselves)
router.put('/profile/:candidateId', [
  body('manifesto').optional().trim(),
  body('photo').optional().trim()
], authenticateToken, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { candidateId } = req.params;
    const updates = req.body;

    // Check if candidate exists
    const candidate = await Candidate.findById(candidateId);
    if (!candidate) {
      return res.status(404).json({ message: 'Candidate not found' });
    }

    // Check if user is updating their own profile
    if (candidate.userId.toString() !== req.user.userId) {
      return res.status(403).json({ message: 'Not authorized to update this candidate profile' });
    }

    // Only allow updating certain fields
    const allowedFields = ['manifesto', 'photo'];
    const filteredUpdates = {};
    allowedFields.forEach(field => {
      if (updates[field] !== undefined) {
        filteredUpdates[field] = updates[field];
      }
    });

    const updatedCandidate = await Candidate.findByIdAndUpdate(
      candidateId,
      filteredUpdates,
      { new: true, runValidators: true }
    ).populate('userId', 'firstName lastName');

    res.json({
      message: 'Candidate profile updated successfully',
      candidate: updatedCandidate
    });
  } catch (error) {
    console.error('Update candidate profile error:', error);
    res.status(500).json({ message: 'Failed to update candidate profile' });
  }
});

// Get candidate statistics
router.get('/:candidateId/stats', async (req, res) => {
  try {
    const { candidateId } = req.params;

    const candidate = await Candidate.findById(candidateId)
      .populate('electionId', 'title totalVoters totalVotes');

    if (!candidate) {
      return res.status(404).json({ message: 'Candidate not found' });
    }

    // Get vote count for this candidate
    const voteCount = candidate.voteCount || 0;
    
    // Calculate percentage if election has votes
    const percentage = candidate.electionId.totalVotes > 0 ? 
      ((voteCount / candidate.electionId.totalVotes) * 100).toFixed(2) : 0;

    // Get constituency ranking
    const constituencyCandidates = await Candidate.find({
      electionId: candidate.electionId._id,
      constituency: candidate.constituency,
      isActive: true
    }).sort({ voteCount: -1 });

    const rank = constituencyCandidates.findIndex(c => c._id.toString() === candidateId) + 1;

    res.json({
      candidate: {
        name: `${candidate.userId.firstName} ${candidate.userId.lastName}`,
        party: candidate.partyName,
        constituency: candidate.constituency
      },
      election: {
        title: candidate.electionId.title,
        totalVoters: candidate.electionId.totalVoters,
        totalVotes: candidate.electionId.totalVotes
      },
      statistics: {
        voteCount,
        percentage: parseFloat(percentage),
        rank,
        totalCandidatesInConstituency: constituencyCandidates.length
      }
    });

  } catch (error) {
    console.error('Get candidate stats error:', error);
    res.status(500).json({ message: 'Failed to fetch candidate statistics' });
  }
});

// Search candidates
router.get('/search', async (req, res) => {
  try {
    const { q, electionId, constituency, party } = req.query;
    
    let query = { isActive: true };
    if (electionId) query.electionId = electionId;
    if (constituency) query.constituency = constituency;
    if (party) query.partyName = { $regex: party, $options: 'i' };

    let candidates = await Candidate.find(query)
      .populate('userId', 'firstName lastName')
      .populate('electionId', 'title');

    // If search query is provided, filter by name or party
    if (q) {
      const searchRegex = new RegExp(q, 'i');
      candidates = candidates.filter(candidate => 
        searchRegex.test(candidate.userId.firstName) ||
        searchRegex.test(candidate.userId.lastName) ||
        searchRegex.test(candidate.partyName)
      );
    }

    res.json(candidates);
  } catch (error) {
    console.error('Search candidates error:', error);
    res.status(500).json({ message: 'Failed to search candidates' });
  }
});

// Get top candidates by vote count
router.get('/top/:limit', async (req, res) => {
  try {
    const { limit } = req.params;
    const { electionId } = req.query;

    let query = { isActive: true };
    if (electionId) query.electionId = electionId;

    const candidates = await Candidate.find(query)
      .populate('userId', 'firstName lastName')
      .populate('electionId', 'title')
      .select('partyName constituency voteCount')
      .sort({ voteCount: -1 })
      .limit(parseInt(limit));

    res.json(candidates);
  } catch (error) {
    console.error('Get top candidates error:', error);
    res.status(500).json({ message: 'Failed to fetch top candidates' });
  }
});

module.exports = router;
