require('dotenv').config();
const { ethers } = require("ethers");
const deployment = require("../deployments/fuji.json");

// ✅ v5-compatible provider
const provider = new ethers.providers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);
const wallet = new ethers.Wallet(process.env.TEST_PRIVATE_KEY, provider);
const contract = new ethers.Contract(
  deployment.contract.address,
  deployment.contract.abi,
  wallet
);

async function addCandidate(name, party, imageHash) {
  try {
    const tx = await contract.addCandidate(name, party, imageHash);
    console.log("Candidate add tx sent:", tx.hash);
    const receipt = await tx.wait();
    console.log("Candidate added in block:", receipt.blockNumber);
  } catch (err) {
    console.error("Error adding candidate:", err);
  }
}

// Example usage: node addCandidate.js "Modi" "BJP" "modi.jpg"
const [name, party, imageHash] = process.argv.slice(2);
if (!name || !party || !imageHash) {
  console.error("Usage: node addCandidate.js <name> <party> <imageHash>");
  process.exit(1);
}
addCandidate(name, party, imageHash);
