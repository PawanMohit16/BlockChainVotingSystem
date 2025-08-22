const mongoose = require('mongoose');

const candidateSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  partyName: {
    type: String,
    required: true,
    trim: true
  },
  partySymbol: {
    type: String, // URL or path to party symbol image
    required: true
  },
  constituency: {
    type: String,
    required: true,
    trim: true
  },
  manifesto: {
    type: String,
    trim: true
  },
  photo: {
    type: String, // URL or path to candidate photo
    required: true
  },
  isActive: {
    type: Boolean,
    default: true
  },
  voteCount: {
    type: Number,
    default: 0
  },
  electionId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Election',
    required: true
  },
  createdAt: {
    type: Date,
    default: Date.now
  },
  updatedAt: {
    type: Date,
    default: Date.now
  }
});

// Update timestamp on save
candidateSchema.pre('save', function(next) {
  this.updatedAt = Date.now();
  next();
});

module.exports = mongoose.model('Candidate', candidateSchema);
