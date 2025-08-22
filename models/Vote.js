const mongoose = require('mongoose');
const crypto = require('crypto');

const voteSchema = new mongoose.Schema({
  voterId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  candidateId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Candidate',
    required: true
  },
  electionId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Election',
    required: true
  },
  constituency: {
    type: String,
    required: true
  },
  timestamp: {
    type: Date,
    default: Date.now
  },
  ipAddress: String,
  userAgent: String,
  // Blockchain verification
  previousHash: String,
  hash: String,
  nonce: Number,
  isVerified: {
    type: Boolean,
    default: false
  },
  verificationTimestamp: Date
});

// Generate hash for blockchain
voteSchema.methods.generateHash = function() {
  const data = this.voterId.toString() + 
               this.candidateId.toString() + 
               this.electionId.toString() + 
               this.timestamp.toISOString() + 
               this.previousHash + 
               this.nonce;
  
  return crypto.createHash('sha256').update(data).digest('hex');
};

// Verify vote hash
voteSchema.methods.verifyHash = function() {
  const calculatedHash = this.generateHash();
  return calculatedHash === this.hash;
};

module.exports = mongoose.model('Vote', voteSchema);
