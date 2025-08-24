require("dotenv").config();
const { ethers } = require("ethers");
const deployment = require("../deployments/fuji.json");

const provider = new ethers.providers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);
const contract = new ethers.Contract(
  deployment.contract.address,
  deployment.contract.abi,
  provider
);

// Fetch and display all active candidates
async function showActiveCandidates() {
  const ids = await contract.getActiveCandidateIds();
  console.log("Active Candidate IDs:", ids);
  for (const id of ids) {
    const candidate = await contract.getCandidate(id);
    console.log(`Candidate #${id}:`, {
      id: candidate[0],
      name: candidate[1],
      party: candidate[2],
      imageHash: candidate[3],
      voteCount: candidate[4],
      isActive: candidate[5]
    });
  }
}

// Test voting functionality
async function testVoting() {
  // Replace with your private key for signing transactions
  const privateKey = process.env.TEST_PRIVATE_KEY;
  if (!privateKey) {
    console.error("Please set TEST_PRIVATE_KEY in your .env file.");
    return;
  }
  const wallet = new ethers.Wallet(privateKey, provider);
  const contractWithSigner = contract.connect(wallet);

  // Get active candidates
  const ids = await contract.getActiveCandidateIds();
  if (ids.length === 0) {
    console.log("No active candidates to vote for.");
    return;
  }
  const candidateId = ids[0];

  // Check if already voted
  const alreadyVoted = await contract.voters(wallet.address);
  console.log("Already voted:", alreadyVoted);

  if (!alreadyVoted) {
    try {
      const tx = await contractWithSigner.vote(candidateId);
      await tx.wait();
      console.log("Vote successful!");
    } catch (err) {
      console.error("Vote failed:", err);
    }
  } else {
    console.log("You have already voted.");
  }
}

showActiveCandidates().then(testVoting);
