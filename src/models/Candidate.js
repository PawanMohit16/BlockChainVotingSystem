const mongoose = require('mongoose');

const CandidateSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Please add a candidate name'],
    trim: true,
  },
  party: {
    type: String,
    required: [true, 'Please add a party name'],
    trim: true,
  },
  photo: {
    type: String,
    required: [true, 'Please add a photo URL'],
  },
  symbol: {
    type: String,
    required: [true, 'Please add a symbol URL'],
  },
  votes: [
    {
      user: {
        type: mongoose.Schema.ObjectId,
        ref: 'User',
        required: true,
      },
      votedAt: {
        type: Date,
        default: Date.now,
      },
    },
  ],
  totalVotes: {
    type: Number,
    default: 0,
  },
  createdAt: {
    type: Date,
    default: Date.now,
  },
});

// Calculate total votes before saving
CandidateSchema.pre('save', function (next) {
  this.totalVotes = this.votes.length;
  next();
});

module.exports = mongoose.model('Candidate', CandidateSchema);
