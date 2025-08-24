const { ethers } = require("hardhat");
const fs = require("fs");
const path = require("path");

async function main() {
  // Load candidates from JSON file
  const candidatesData = JSON.parse(
    fs.readFileSync(path.join(__dirname, "../candidates.json"), "utf-8")
  );

  // Get contract instance
  const contractAddress = process.env.CONTRACT_ADDRESS;
  if (!contractAddress) {
    throw new Error("CONTRACT_ADDRESS environment variable is not set");
  }

  const Voting = await ethers.getContractFactory("Voting");
  const voting = await Voting.attach(contractAddress);

  console.log(`Managing candidates for contract: ${contractAddress}`);
  console.log(`Current owner: ${await voting.owner()}`);

  // Get current active candidates
  const activeIds = await voting.getActiveCandidateIds();
  console.log(`Current active candidates: ${activeIds.length}`);

  // Add new candidates
  for (const candidate of candidatesData.candidates) {
    console.log(`\nProcessing candidate: ${candidate.name}`);
    
    // Check if candidate with same name already exists
    let candidateExists = false;
    for (const id of activeIds) {
      const [id, name] = await voting.getCandidate(id);
      if (name === candidate.name) {
        console.log(`✅ Candidate "${candidate.name}" already exists`);
        candidateExists = true;
        break;
      }
    }

    if (!candidateExists) {
      console.log(`➕ Adding candidate: ${candidate.name} (${candidate.party})`);
      const tx = await voting.addCandidate(
        candidate.name,
        candidate.party,
        candidate.imageHash
      );
      await tx.wait();
      console.log(`✅ Added candidate: ${candidate.name}`);
    }
  }

  console.log("\nCandidate management completed!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("\n=== Error in candidate management ===\n");
    console.error(error);
    process.exit(1);
  });
