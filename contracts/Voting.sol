// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract Voting {
    // Structure to store candidate details
    struct Candidate {
        uint256 id;
        string name;
        string party;
        string imageHash; // IPFS hash for candidate image
        uint256 voteCount;
        bool isActive;
    }

    // Contract owner (admin)
    address public owner;
    
    // Mapping to store candidates
    mapping(uint256 => Candidate) public candidates;
    
    // Track total candidates
    uint256 public candidatesCount;
    
    // Track who has voted
    mapping(address => bool) public voters;
    
    // Events
    event CandidateAdded(uint256 indexed candidateId, string name, string party);
    event CandidateRemoved(uint256 indexed candidateId);
    event VotedEvent(uint256 indexed candidateId, address voter);
    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    // Modifier to restrict access to owner
    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can perform this action");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    // Transfer ownership to another address
    function transferOwnership(address newOwner) public onlyOwner {
        require(newOwner != address(0), "New owner cannot be the zero address");
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }

    // Add a new candidate (only owner)
    function addCandidate(
        string memory _name,
        string memory _party,
        string memory _imageHash
    ) public onlyOwner {
        candidatesCount++;
        candidates[candidatesCount] = Candidate(
            candidatesCount,
            _name,
            _party,
            _imageHash,
            0,
            true
        );
        emit CandidateAdded(candidatesCount, _name, _party);
    }

    // Remove a candidate (only owner, doesn't delete, just marks as inactive)
    function removeCandidate(uint256 _candidateId) public onlyOwner {
        require(_candidateId <= candidatesCount && _candidateId > 0, "Invalid candidate ID");
        require(candidates[_candidateId].isActive, "Candidate already inactive");
        
        candidates[_candidateId].isActive = false;
        emit CandidateRemoved(_candidateId);
    }

    // Cast a vote
    function vote(uint256 _candidateId) public {
        // Require that the voter hasn't voted before
        require(!voters[msg.sender], "You have already voted");
        
        // Require a valid and active candidate
        require(_candidateId > 0 && _candidateId <= candidatesCount, "Invalid candidate");
        require(candidates[_candidateId].isActive, "Candidate is not active");
        
        // Record that voter has voted
        voters[msg.sender] = true;
        
        // Update candidate vote count
        candidates[_candidateId].voteCount++;
        
        // Trigger voted event
        emit VotedEvent(_candidateId, msg.sender);
    }
    
    // Get candidate details
    function getCandidate(uint256 _candidateId) public view returns (
        uint256 id,
        string memory name,
        string memory party,
        string memory imageHash,
        uint256 voteCount,
        bool isActive
    ) {
        require(_candidateId <= candidatesCount && _candidateId > 0, "Invalid candidate ID");
        Candidate memory c = candidates[_candidateId];
        return (c.id, c.name, c.party, c.imageHash, c.voteCount, c.isActive);
    }
    
    // Get total votes for a candidate
    function getVotes(uint256 _candidateId) public view returns (uint256) {
        require(_candidateId > 0 && _candidateId <= candidatesCount, "Invalid candidate");
        return candidates[_candidateId].voteCount;
    }
    
    // Get the winning candidate ID
    function winningCandidate() public view returns (uint256) {
        uint256 winningVoteCount = 0;
        uint256 winningCandidateId = 0;
        
        for (uint256 i = 1; i <= candidatesCount; i++) {
            if (candidates[i].isActive && candidates[i].voteCount > winningVoteCount) {
                winningVoteCount = candidates[i].voteCount;
                winningCandidateId = i;
            }
        }
        
        return winningCandidateId;
    }
    
    // Get total number of active candidates
    function getActiveCandidatesCount() public view returns (uint256) {
        uint256 count = 0;
        for (uint256 i = 1; i <= candidatesCount; i++) {
            if (candidates[i].isActive) {
                count++;
            }
        }
        return count;
    }
    
    // Get list of active candidate IDs
    function getActiveCandidateIds() public view returns (uint256[] memory) {
        uint256 activeCount = getActiveCandidatesCount();
        uint256[] memory activeIds = new uint256[](activeCount);
        uint256 currentIndex = 0;
        
        for (uint256 i = 1; i <= candidatesCount; i++) {
            if (candidates[i].isActive) {
                activeIds[currentIndex] = i;
                currentIndex++;
            }
        }
        
        return activeIds;
    }
}
