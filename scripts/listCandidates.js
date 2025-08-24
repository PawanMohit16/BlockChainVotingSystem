require('dotenv').config();
const { ethers } = require("ethers");
const deployment = require("../deployments/fuji.json");

const provider = new ethers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);
const contract = new ethers.Contract(
  deployment.contract.address,
  deployment.contract.abi,
  provider
);

async function listCandidates() {
  try {
    const ids = await contract.getActiveCandidateIds();
    console.log("Active candidate IDs:", ids);
    for (const id of ids) {
      const candidate = await contract.getCandidate(id);
      console.log(`ID: ${candidate.id} | Name: ${candidate.name} | Party: ${candidate.party} | Votes: ${candidate.voteCount}`);
    }
  } catch (err) {
    console.error("Error fetching candidates:", err);
  }
}

listCandidates();
const { ethers } = require("ethers");
const provider = new ethers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);const { ethers } = require("ethers");
const provider = new ethers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);const { ethers } = require("ethers");
const provider = new ethers.JsonRpcProvider(process.env.AVALANCHE_RPC_URL);