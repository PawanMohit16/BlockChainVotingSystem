const crypto = require('crypto');

class BlockchainService {
  constructor() {
    this.chain = [];
    this.pendingVotes = [];
    this.difficulty = 4; // Number of leading zeros required in hash
    this.miningReward = 0; // No mining reward for voting system
  }

  // Initialize blockchain with genesis block
  initialize() {
    try {
      // Create genesis block
      const genesisBlock = {
        index: 0,
        timestamp: Date.now(),
        votes: [],
        previousHash: '0',
        hash: this.calculateHash(0, Date.now(), [], '0', 0),
        nonce: 0
      };

      this.chain = [genesisBlock];
      console.log('Blockchain initialized with genesis block');
      return true;
    } catch (error) {
      console.error('Failed to initialize blockchain:', error);
      return false;
    }
  }

  // Calculate hash for a block
  calculateHash(index, timestamp, votes, previousHash, nonce) {
    try {
      const data = index + timestamp + JSON.stringify(votes) + previousHash + nonce;
      return crypto.createHash('sha256').update(data).toString('hex');
    } catch (error) {
      console.error('Hash calculation failed:', error);
      throw new Error('Hash calculation failed');
    }
  }

  // Get the latest block in the chain
  getLatestBlock() {
    return this.chain[this.chain.length - 1];
  }

  // Add a new vote to pending votes
  addVote(vote) {
    try {
      // Validate vote structure
      if (!this.validateVote(vote)) {
        throw new Error('Invalid vote structure');
      }

      // Check if vote already exists
      const existingVote = this.pendingVotes.find(v => v.voteId === vote.voteId);
      if (existingVote) {
        throw new Error('Vote already exists in pending votes');
      }

      // Add vote to pending list
      this.pendingVotes.push(vote);
      console.log(`Vote ${vote.voteId} added to pending votes`);
      
      return true;
    } catch (error) {
      console.error('Failed to add vote:', error);
      return false;
    }
  }

  // Validate vote structure
  validateVote(vote) {
    try {
      // Check required fields
      if (!vote.voterId || !vote.candidateId || !vote.electionId || !vote.timestamp) {
        return false;
      }

      // Check if vote has a valid hash
      if (!vote.hash || typeof vote.hash !== 'string') {
        return false;
      }

      // Check if vote has previous hash
      if (!vote.previousHash || typeof vote.previousHash !== 'string') {
        return false;
      }

      // Verify vote hash
      const calculatedHash = this.calculateVoteHash(vote);
      if (calculatedHash !== vote.hash) {
        return false;
      }

      return true;
    } catch (error) {
      console.error('Vote validation failed:', error);
      return false;
    }
  }

  // Calculate hash for a single vote
  calculateVoteHash(vote) {
    try {
      const data = vote.voterId.toString() + 
                   vote.candidateId.toString() + 
                   vote.electionId.toString() + 
                   vote.timestamp.toISOString() + 
                   vote.previousHash + 
                   vote.nonce;
      
      return crypto.createHash('sha256').update(data).toString('hex');
    } catch (error) {
      console.error('Vote hash calculation failed:', error);
      throw new Error('Vote hash calculation failed');
    }
  }

  // Mine a new block with pending votes
  async mineBlock() {
    try {
      if (this.pendingVotes.length === 0) {
        console.log('No pending votes to mine');
        return null;
      }

      const previousBlock = this.getLatestBlock();
      const newBlockIndex = previousBlock.index + 1;
      const newBlockTimestamp = Date.now();
      const newBlockVotes = [...this.pendingVotes];
      
      // Clear pending votes
      this.pendingVotes = [];

      // Find nonce that satisfies difficulty requirement
      let nonce = 0;
      let newBlockHash = '';
      
      do {
        newBlockHash = this.calculateHash(
          newBlockIndex,
          newBlockTimestamp,
          newBlockVotes,
          previousBlock.hash,
          nonce
        );
        nonce++;
      } while (!newBlockHash.startsWith('0'.repeat(this.difficulty)));

      // Create new block
      const newBlock = {
        index: newBlockIndex,
        timestamp: newBlockTimestamp,
        votes: newBlockVotes,
        previousHash: previousBlock.hash,
        hash: newBlockHash,
        nonce: nonce - 1
      };

      // Add block to chain
      this.chain.push(newBlock);
      
      console.log(`Block ${newBlockIndex} mined successfully with ${newBlockVotes.length} votes`);
      console.log(`Block hash: ${newBlockHash}`);
      
      return newBlock;
    } catch (error) {
      console.error('Block mining failed:', error);
      return null;
    }
  }

  // Verify vote integrity
  async verifyVote(vote) {
    try {
      // Check if vote exists in any block
      const voteInBlock = this.findVoteInBlockchain(vote.voteId);
      
      if (!voteInBlock) {
        return {
          isValid: false,
          error: 'Vote not found in blockchain',
          verified: false
        };
      }

      // Verify vote hash
      const calculatedHash = this.calculateVoteHash(vote);
      if (calculatedHash !== vote.hash) {
        return {
          isValid: false,
          error: 'Vote hash verification failed',
          verified: false
        };
      }

      // Check if vote is in a valid block
      const blockIndex = voteInBlock.blockIndex;
      if (!this.isBlockValid(blockIndex)) {
        return {
          isValid: false,
          error: 'Vote block is invalid',
          verified: false
        };
      }

      return {
        isValid: true,
        verified: true,
        blockIndex: blockIndex,
        blockHash: this.chain[blockIndex].hash
      };
    } catch (error) {
      console.error('Vote verification failed:', error);
      return {
        isValid: false,
        error: 'Vote verification failed',
        verified: false
      };
    }
  }

  // Find vote in blockchain
  findVoteInBlockchain(voteId) {
    try {
      for (let i = 0; i < this.chain.length; i++) {
        const block = this.chain[i];
        const vote = block.votes.find(v => v.voteId === voteId);
        if (vote) {
          return {
            vote: vote,
            blockIndex: i,
            blockHash: block.hash
          };
        }
      }
      return null;
    } catch (error) {
      console.error('Failed to find vote in blockchain:', error);
      return null;
    }
  }

  // Check if a block is valid
  isBlockValid(blockIndex) {
    try {
      if (blockIndex < 0 || blockIndex >= this.chain.length) {
        return false;
      }

      const block = this.chain[blockIndex];
      
      // Check if block hash is correct
      const calculatedHash = this.calculateHash(
        block.index,
        block.timestamp,
        block.votes,
        block.previousHash,
        block.nonce
      );

      if (calculatedHash !== block.hash) {
        return false;
      }

      // Check if hash meets difficulty requirement
      if (!block.hash.startsWith('0'.repeat(this.difficulty))) {
        return false;
      }

      // Check if previous hash is correct (except for genesis block)
      if (blockIndex > 0) {
        const previousBlock = this.chain[blockIndex - 1];
        if (block.previousHash !== previousBlock.hash) {
          return false;
        }
      }

      return true;
    } catch (error) {
      console.error('Block validation failed:', error);
      return false;
    }
  }

  // Verify entire blockchain integrity
  verifyBlockchain() {
    try {
      for (let i = 0; i < this.chain.length; i++) {
        if (!this.isBlockValid(i)) {
          return {
            isValid: false,
            error: `Block ${i} is invalid`,
            invalidBlockIndex: i
          };
        }
      }

      return {
        isValid: true,
        message: 'Blockchain integrity verified',
        totalBlocks: this.chain.length
      };
    } catch (error) {
      console.error('Blockchain verification failed:', error);
      return {
        isValid: false,
        error: 'Blockchain verification failed'
      };
    }
  }

  // Get blockchain statistics
  getBlockchainStats() {
    try {
      const totalVotes = this.chain.reduce((sum, block) => sum + block.votes.length, 0);
      const totalBlocks = this.chain.length;
      const pendingVotes = this.pendingVotes.length;
      
      // Calculate average mining time
      let totalMiningTime = 0;
      for (let i = 1; i < this.chain.length; i++) {
        totalMiningTime += this.chain[i].timestamp - this.chain[i - 1].timestamp;
      }
      const averageMiningTime = totalBlocks > 1 ? totalMiningTime / (totalBlocks - 1) : 0;

      return {
        totalBlocks,
        totalVotes,
        pendingVotes,
        difficulty: this.difficulty,
        averageMiningTime: Math.round(averageMiningTime),
        chainHeight: totalBlocks - 1,
        lastBlockHash: totalBlocks > 0 ? this.chain[totalBlocks - 1].hash : null
      };
    } catch (error) {
      console.error('Failed to get blockchain stats:', error);
      return null;
    }
  }

  // Get votes for a specific election
  getElectionVotes(electionId) {
    try {
      const electionVotes = [];
      
      for (const block of this.chain) {
        for (const vote of block.votes) {
          if (vote.electionId.toString() === electionId.toString()) {
            electionVotes.push({
              ...vote,
              blockIndex: block.index,
              blockHash: block.hash,
              blockTimestamp: block.timestamp
            });
          }
        }
      }

      return electionVotes;
    } catch (error) {
      console.error('Failed to get election votes:', error);
      return [];
    }
  }

  // Get blockchain as JSON
  getBlockchainJSON() {
    try {
      return {
        chain: this.chain,
        pendingVotes: this.pendingVotes,
        difficulty: this.difficulty,
        stats: this.getBlockchainStats()
      };
    } catch (error) {
      console.error('Failed to get blockchain JSON:', error);
      return null;
    }
  }

  // Test blockchain service
  async test() {
    try {
      // Initialize blockchain
      const initialized = this.initialize();
      if (!initialized) {
        return {
          success: false,
          error: 'Failed to initialize blockchain'
        };
      }

      // Test vote creation
      const testVote = {
        voteId: 'test-vote-1',
        voterId: 'test-voter-1',
        candidateId: 'test-candidate-1',
        electionId: 'test-election-1',
        timestamp: new Date(),
        previousHash: this.getLatestBlock().hash,
        nonce: 0
      };

      // Calculate hash for test vote
      testVote.hash = this.calculateVoteHash(testVote);

      // Add vote to pending
      const voteAdded = this.addVote(testVote);
      if (!voteAdded) {
        return {
          success: false,
          error: 'Failed to add test vote'
        };
      }

      // Mine block
      const minedBlock = await this.mineBlock();
      if (!minedBlock) {
        return {
          success: false,
          error: 'Failed to mine test block'
        };
      }

      // Verify blockchain integrity
      const integrityCheck = this.verifyBlockchain();
      if (!integrityCheck.isValid) {
        return {
          success: false,
          error: integrityCheck.error
        };
      }

      return {
        success: true,
        message: 'Blockchain service is working properly',
        stats: this.getBlockchainStats()
      };
    } catch (error) {
      console.error('Blockchain service test failed:', error);
      return {
        success: false,
        error: error.message
      };
    }
  }
}

module.exports = new BlockchainService();
