const express = require('express');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const User = require('../models/User');
const Candidate = require('../models/Candidate');
const Election = require('../models/Election');
const Vote = require('../models/Vote');

const router = express.Router();

// JWT Secret
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';

// Middleware to verify JWT token and admin role
const authenticateAdmin = async (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Access token required' });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = await User.findById(decoded.userId);
    
    if (!user || user.role !== 'admin') {
      return res.status(403).json({ message: 'Admin access required' });
    }
    
    req.user = user;
    next();
  } catch (err) {
    return res.status(403).json({ message: 'Invalid or expired token' });
  }
};

// Get all pending user approvals
router.get('/pending-users', authenticateAdmin, async (req, res) => {
  try {
    const pendingUsers = await User.find({ 
      isVerified: true, 
      isApproved: false,
      role: 'voter'
    }).select('firstName lastName email phone voterId dateOfBirth createdAt');

    res.json(pendingUsers);
  } catch (error) {
    console.error('Get pending users error:', error);
    res.status(500).json({ message: 'Failed to fetch pending users' });
  }
});

// Approve user
router.post('/approve-user/:userId', authenticateAdmin, async (req, res) => {
  try {
    const { userId } = req.params;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    if (user.isApproved) {
      return res.status(400).json({ message: 'User is already approved' });
    }

    user.isApproved = true;
    await user.save();

    // Send approval email
    await EmailService.sendApprovalEmail(user.email, user.firstName);

    res.json({ message: 'User approved successfully' });
  } catch (error) {
    console.error('Approve user error:', error);
    res.status(500).json({ message: 'Failed to approve user' });
  }
});

// Reject user
router.post('/reject-user/:userId', [
  body('reason').notEmpty().trim()
], authenticateAdmin, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { userId } = req.params;
    const { reason } = req.body;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    // Send rejection email
    await EmailService.sendRejectionEmail(user.email, user.firstName, reason);

    // Delete user
    await User.findByIdAndDelete(userId);

    res.json({ message: 'User rejected and removed successfully' });
  } catch (error) {
    console.error('Reject user error:', error);
    res.status(500).json({ message: 'Failed to reject user' });
  }
});

// Create new election
router.post('/elections', [
  body('title').notEmpty().trim(),
  body('description').notEmpty().trim(),
  body('startDate').isISO8601(),
  body('endDate').isISO8601(),
  body('constituencies').isArray().notEmpty()
], authenticateAdmin, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { title, description, startDate, endDate, constituencies } = req.body;

    // Validate dates
    if (new Date(startDate) >= new Date(endDate)) {
      return res.status(400).json({ message: 'End date must be after start date' });
    }

    const election = new Election({
      title,
      description,
      startDate,
      endDate,
      constituencies: constituencies.map(c => ({ name: c, totalVoters: 0, votedCount: 0 }))
    });

    await election.save();

    res.status(201).json({
      message: 'Election created successfully',
      electionId: election._id
    });
  } catch (error) {
    console.error('Create election error:', error);
    res.status(500).json({ message: 'Failed to create election' });
  }
});

// Update election
router.put('/elections/:electionId', [
  body('title').optional().trim(),
  body('description').optional().trim(),
  body('startDate').optional().isISO8601(),
  body('endDate').optional().isISO8601(),
  body('isActive').optional().isBoolean()
], authenticateAdmin, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { electionId } = req.params;
    const updates = req.body;

    const election = await Election.findById(electionId);
    if (!election) {
      return res.status(404).json({ message: 'Election not found' });
    }

    // Validate dates if updating
    if (updates.startDate && updates.endDate) {
      if (new Date(updates.startDate) >= new Date(updates.endDate)) {
        return res.status(400).json({ message: 'End date must be after start date' });
      }
    }

    Object.assign(election, updates);
    await election.save();

    res.json({ message: 'Election updated successfully' });
  } catch (error) {
    console.error('Update election error:', error);
    res.status(500).json({ message: 'Failed to update election' });
  }
});

// Add candidate to election
router.post('/elections/:electionId/candidates', [
  body('userId').isMongoId(),
  body('partyName').notEmpty().trim(),
  body('constituency').notEmpty().trim(),
  body('manifesto').optional().trim()
], authenticateAdmin, async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { electionId } = req.params;
    const { userId, partyName, constituency, manifesto } = req.body;

    // Check if election exists
    const election = await Election.findById(electionId);
    if (!election) {
      return res.status(404).json({ message: 'Election not found' });
    }

    // Check if user exists and is verified
    const user = await User.findById(userId);
    if (!user || !user.isVerified) {
      return res.status(400).json({ message: 'User not found or not verified' });
    }

    // Check if constituency exists in election
    const constituencyExists = election.constituencies.some(c => c.name === constituency);
    if (!constituencyExists) {
      return res.status(400).json({ message: 'Invalid constituency for this election' });
    }

    // Check if candidate already exists for this constituency
    const existingCandidate = await Candidate.findOne({
      electionId,
      constituency,
      isActive: true
    });

    if (existingCandidate) {
      return res.status(400).json({ message: 'Candidate already exists for this constituency' });
    }

    const candidate = new Candidate({
      userId,
      partyName,
      constituency,
      manifesto,
      electionId
    });

    await candidate.save();

    res.status(201).json({
      message: 'Candidate added successfully',
      candidateId: candidate._id
    });
  } catch (error) {
    console.error('Add candidate error:', error);
    res.status(500).json({ message: 'Failed to add candidate' });
  }
});

// Get election statistics
router.get('/elections/:electionId/stats', authenticateAdmin, async (req, res) => {
  try {
    const { electionId } = req.params;

    const election = await Election.findById(electionId);
    if (!election) {
      return res.status(404).json({ message: 'Election not found' });
    }

    const candidates = await Candidate.find({ electionId });
    const totalVotes = await Vote.countDocuments({ electionId });
    const uniqueVoters = await Vote.distinct('voterId', { electionId });

    const constituencyStats = await Promise.all(
      election.constituencies.map(async (constituency) => {
        const constituencyVotes = await Vote.countDocuments({ 
          electionId, 
          constituency: constituency.name 
        });
        const constituencyCandidates = candidates.filter(c => c.constituency === constituency.name);
        
        return {
          name: constituency.name,
          totalVoters: constituency.totalVoters,
          votedCount: constituencyVotes,
          candidates: constituencyCandidates.length,
          turnout: constituency.totalVoters > 0 ? 
            ((constituencyVotes / constituency.totalVoters) * 100).toFixed(2) : 0
        };
      })
    );

    res.json({
      election: {
        title: election.title,
        startDate: election.startDate,
        endDate: election.endDate,
        isActive: election.isActive
      },
      statistics: {
        totalVoters: election.totalVoters,
        totalVotes,
        uniqueVoters: uniqueVoters.length,
        overallTurnout: election.totalVoters > 0 ? 
          ((totalVotes / election.totalVoters) * 100).toFixed(2) : 0
      },
      constituencies: constituencyStats
    });

  } catch (error) {
    console.error('Get election stats error:', error);
    res.status(500).json({ message: 'Failed to fetch election statistics' });
  }
});

// Get all users
router.get('/users', authenticateAdmin, async (req, res) => {
  try {
    const { page = 1, limit = 10, role, status } = req.query;
    
    let query = {};
    if (role) query.role = role;
    if (status === 'pending') {
      query.isVerified = true;
      query.isApproved = false;
    } else if (status === 'approved') {
      query.isApproved = true;
    } else if (status === 'verified') {
      query.isVerified = true;
    }

    const users = await User.find(query)
      .select('firstName lastName email username role isVerified isApproved hasVoted createdAt')
      .limit(limit * 1)
      .skip((page - 1) * limit)
      .sort({ createdAt: -1 });

    const total = await User.countDocuments(query);

    res.json({
      users,
      totalPages: Math.ceil(total / limit),
      currentPage: page,
      total
    });
  } catch (error) {
    console.error('Get users error:', error);
    res.status(500).json({ message: 'Failed to fetch users' });
  }
});

module.exports = router;
