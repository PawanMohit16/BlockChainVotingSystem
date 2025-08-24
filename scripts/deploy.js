const { ethers } = require("hardhat");
const fs = require("fs");
const path = require("path");

async function main() {
  // Compile the contract
  console.log("Compiling contracts...");
  await hre.run("compile");
  
  // Get the contract factory
  const Voting = await ethers.getContractFactory("Voting");
  
  console.log("Deploying Voting contract...");
  
  // Deploy the contract
  const voting = await Voting.deploy();
  await voting.deployed();
  
  console.log("\n=== Deployment Successful ===");
  console.log(`Contract Address: ${voting.address}`);
  console.log(`Owner: ${await voting.owner()}`);
  console.log(`Transaction Hash: ${voting.deployTransaction.hash}`);
  
  // Save deployment info to a file
  const deploymentInfo = {
    network: hre.network.name,
    contract: {
      address: voting.address,
      abi: JSON.parse(voting.interface.format('json')),
      deployTransaction: voting.deployTransaction.hash
    },
    timestamp: new Date().toISOString()
  };
  
  const deploymentDir = path.join(__dirname, "../deployments");
  if (!fs.existsSync(deploymentDir)) {
    fs.mkdirSync(deploymentDir, { recursive: true });
  }
  
  const deploymentFile = path.join(deploymentDir, `${hre.network.name}.json`);
  fs.writeFileSync(deploymentFile, JSON.stringify(deploymentInfo, null, 2));
  
  console.log(`\nDeployment info saved to: ${deploymentFile}`);
  console.log("\nNext steps:");
  console.log(`1. Set CONTRACT_ADDRESS=${voting.address} in your .env file`);
  console.log("2. Update candidates in candidates.json if needed");
  console.log("3. Run: npx hardhat run scripts/manage-candidates.js --network fuji");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("\n=== Deployment Failed ===\n");
    console.error(error);
    process.exit(1);
  });
