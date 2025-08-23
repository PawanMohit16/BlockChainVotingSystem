const Candidate = require('../models/Candidate');
const User = require('../models/User');
const ErrorResponse = require('../utils/errorResponse');
const asyncHandler = require('../middleware/async');

// @desc    Cast a vote
// @route   POST /api/v1/votes
// @access  Private
exports.castVote = asyncHandler(async (req, res, next) => {
  const { candidateId } = req.body;
  const userId = req.user.id;

  // Check if user has already voted
  const user = await User.findById(userId);
  if (user.hasVoted) {
    return next(new ErrorResponse('You have already voted', 400));
  }

  // Find the candidate
  const candidate = await Candidate.findById(candidateId);
  if (!candidate) {
    return next(new ErrorResponse(`No candidate with id ${candidateId}`, 404));
  }

  // Add vote to candidate
  candidate.votes.push({
    user: userId,
    votedAt: Date.now(),
  });

  // Update total votes
  candidate.totalVotes = candidate.votes.length;
  await candidate.save();

  // Mark user as voted
  user.hasVoted = true;
  user.votedFor = candidateId;
  user.votedAt = Date.now();
  await user.save();

  res.status(200).json({
    success: true,
    data: {},
  });
});

// @desc    Get voting results
// @route   GET /api/v1/votes/results
// @access  Private/Admin
exports.getResults = asyncHandler(async (req, res, next) => {
  const candidates = await Candidate.find().sort({ totalVotes: -1 });
  const totalVotes = candidates.reduce(
    (sum, candidate) => sum + candidate.totalVotes,
    0
  );

  // Add percentage to each candidate
  const results = candidates.map((candidate) => ({
    ...candidate.toObject(),
    votePercentage:
      totalVotes > 0 ? (candidate.totalVotes / totalVotes) * 100 : 0,
  }));

  res.status(200).json({
    success: true,
    count: candidates.length,
    totalVotes,
    data: results,
  });
});

// @desc    Get user's vote
// @route   GET /api/v1/votes/my-vote
// @access  Private
exports.getUserVote = asyncHandler(async (req, res, next) => {
  const user = await User.findById(req.user.id).populate('votedFor', 'name party');
  
  if (!user.hasVoted) {
    return res.status(200).json({
      success: true,
      data: { hasVoted: false },
    });
  }

  res.status(200).json({
    success: true,
    data: {
      hasVoted: true,
      candidate: user.votedFor,
      votedAt: user.votedAt,
    },
  });
});
