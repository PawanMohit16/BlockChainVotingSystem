require('dotenv').config();
const { ethers } = require("ethers");
const deployment = require("../deployments/fuji.json");

const provider = new ethers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);
const wallet = new ethers.Wallet(process.env.TEST_PRIVATE_KEY, provider);
const contract = new ethers.Contract(
  deployment.contract.address,
  deployment.contract.abi,
  wallet
);

async function vote(candidateId) {
  try {
    const tx = await contract.vote(candidateId);
    console.log("Vote transaction sent! Hash:", tx.hash);
    const receipt = await tx.wait();
    console.log("Vote confirmed in block:", receipt.blockNumber);
  } catch (err) {
    if (err.message.includes('already voted')) {
      console.log("You have already voted.");
    } else {
      console.error("Voting failed:", err);
    }
  }
}

// Example usage: node vote.js 1
const candidateId = process.argv[2];
if (!candidateId) {
  console.error("Usage: node vote.js <candidateId>");
  process.exit(1);
}
vote(candidateId);
