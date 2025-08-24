package net.codejava.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import net.codejava.model.Block;
import net.codejava.model.Votedata;
import net.codejava.repository.VoteRepo;
import net.codejava.smartcontract.VoteSmartContract;
import net.codejava.model.Candidate;
import net.codejava.model.User;
import net.codejava.repository.UserRepo;

@Service
public class VoteService {
    private final AtomicBoolean isVotingActive = new AtomicBoolean(true);

    @Autowired
    private VoteRepo voterepo;

    @Autowired
    private VoteSmartContract smartcontract;

    @Autowired
    private net.codejava.repository.CandidateRepo candidaterepo;

    @Autowired
    private UserRepo userRepo;

        @Autowired
        private net.codejava.smartcontract.VotingContractService votingContractService;

        @PostConstruct
        private void initVotingStateFromAdmin() {
            try {
                User admin = userRepo.findByUsername("admin");
                if (admin != null) {
                    String vs = admin.getVotestatus();
                    // votestatus: "1" = open, else closed
                    boolean active = "1".equals(vs);
                    isVotingActive.set(active);
                    System.out.println("[INIT] Voting active set to: " + active + " from admin votestatus=" + vs);
                }
            } catch (Exception e) {
                System.err.println("[INIT] Failed to initialize voting state from admin: " + e.getMessage());
            }
        }
    
    // Removed unused mongoTemplate for now
    
    // Voting control methods
    public void setVotingActive(boolean active) {
        isVotingActive.set(active);
    }
    
    public boolean isVotingActive() {
        return isVotingActive.get();
    }
    
    public void resetVotingSystem() {
        // Clear all votes
        voterepo.deleteAll();
        // Reset voting status
        isVotingActive.set(false);
        
        // Reset all candidate vote counts
        List<Candidate> candidates = candidaterepo.findAll();
        for (Candidate candidate : candidates) {
            candidate.setVoteCount(0);
            candidaterepo.save(candidate);
        }

        // Reset voted status for all users
        List<User> users = userRepo.findAll();
        for (User user : users) {
            user.setVoted(false);
            userRepo.save(user);
        }
    }
    
    public Map<String, Object> getVotingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get votes from candidates
        List<Candidate> candidates = candidaterepo.findAll();
        long totalVotes = 0;
        Map<String, Long> votesByCandidate = new HashMap<>();
        
        // Calculate total votes and votes per candidate
        for (Candidate candidate : candidates) {
            long votes = candidate.getVoteCount();
            totalVotes += votes;
            votesByCandidate.put(candidate.getParty(), votes);
        }
        
        stats.put("totalVotes", totalVotes);
        
        // Add vote counts by candidate
        stats.put("votesByCandidate", votesByCandidate);
        
        // Add voting status
        stats.put("isVotingActive", isVotingActive.get());
        
        // Add timestamp
        stats.put("lastUpdated", new Date());
        
        return stats;
    }

    public boolean isSuccessfull(String candidateName, String adhhar, String name)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        // Check if voting is active
        // Check if voting is active
        if (!isVotingActive()) {
            throw new IllegalStateException("Voting is not active at this time");
        }
        // Check if user has already voted
        if (userExists(adhhar)) {
            throw new IllegalStateException("You have already voted");
        }

        // Check on blockchain if user has already voted
        try {
            Boolean hasVoted = votingContractService.hasVoted(adhhar);
            if (hasVoted != null && hasVoted) {
                throw new IllegalStateException("You have already voted (blockchain)");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Blockchain check failed: " + e.getMessage());
            throw new RuntimeException("Blockchain check failed: " + e.getMessage());
        }

        // Prepare vote block
        Votedata lastEntry = voterepo.findTopByOrderByDateDesc();
        String[] data = { adhhar, name, candidateName };
        Block block = (lastEntry == null) ? new Block(data, "0") : new Block(data, lastEntry.getCurrhash());

        Votedata vote = new Votedata();
        vote.setUsername(adhhar);
        vote.setCandidate(candidateName);
        vote.setCurrhash(block.getBlockHash());
        vote.setPrevhash(block.getPreviousBlockHash());
        vote.setDate(new Date());

        // Save vote in DB
        voterepo.save(vote);
        System.out.println("[INFO] Vote saved for user: " + adhhar + " candidate: " + candidateName);

        // Update candidate vote count
        Candidate candidate = candidaterepo.findByParty(candidateName);
        if (candidate != null) {
            candidate.incrementVoteCount();
            candidaterepo.save(candidate);
            System.out.println("[INFO] Candidate " + candidateName + " vote count incremented.");
        } else {
            System.err.println("[ERROR] Invalid candidate: " + candidateName);
            throw new IllegalStateException("Invalid candidate: " + candidateName);
        }

        // Record vote on blockchain and log transaction hash
        try {
            // TODO: Map candidateName to candidateId
            java.math.BigInteger candidateId = java.math.BigInteger.valueOf(1); // Replace with actual mapping
            org.web3j.protocol.core.methods.response.TransactionReceipt receipt = votingContractService.vote(candidateId);
            String txHash = receipt.getTransactionHash();
            vote.setCurrhash(txHash); // Optionally store txHash in currhash or add a new field for txHash
            voterepo.save(vote); // Update with txHash
            System.out.println("[BLOCKCHAIN] Vote transaction hash: " + txHash);
            System.out.println("[BLOCKCHAIN] Transaction status: " + receipt.getStatus());
        } catch (Exception e) {
            // Check if error is "already voted" - log but continue for testing
            if (e.getMessage().contains("execution reverted: You have already voted")) {
                System.out.println("[BLOCKCHAIN] User has already voted on-chain - allowing DB update for testing");
            } else {
                System.err.println("[ERROR] Blockchain vote failed: " + e.getMessage());
                throw new RuntimeException("Blockchain vote failed: " + e.getMessage());
            }
        }

    return true;
    }

    public boolean userExists(String username) {
        Votedata user = voterepo.findByUsername(username);
        return user != null;
    }

    public int countVotes() {
        return (int) voterepo.count();
    }

    // public static void pollVotes(String choiced, String adhaarid, String uname) {

    //     ArrayList<Block> blockchain1 = new ArrayList<Block>();

    //     Scanner sc = new Scanner(System.in);

    //     // first blockchain for First party
    //     // Array = adhar id + first name + choice party

    //     // System.out.println("first block is "+firstPartyBlock.toString());

    //     /*
    //      * // first blockchain for Second party
    //      * String[] initialSecondPartyValues={"000000000000","BJP"};
    //      * int secondPartyHash=0001;
    //      * Block secondPartyBlock = new Block(initialSecondPartyValues,secondPartyHash);
    //      * secondPartyHash=secondPartyBlock.getBlockHash();
    //      * //System.out.println(secondPartyBlock.hashCode());
    //      * blockchain2.add(secondPartyBlock);
    //      */
    //     // System.out.println("first block is "+secondPartyBlock.toString());

    //     // System.out.println("Enter your Adhar id");
    //     String adhaar = adhaarid;

    //     // System.out.println("Enter your First Name");
    //     String name = uname;

    //     // System.out.println("Enter which party you want to vote");
    //     String choice = choiced;
    //     long candidateHash = 0;
    //     String[] FirstPartyValues = { adhaar, name, choice };
    //     Block candidate = new Block(FirstPartyValues, candidateHash);
    //     candidateHash = candidate.getBlockHash();

    //     blockchain1.add(candidate);
    //     /*
    //      * if (name.equals("TMC")) {
    //      * String[] FirstPartyValues = {adhaar, name};
    //      * Block firstParty = new Block(FirstPartyValues, firstPartyHash);
    //      * firstPartyHash = firstParty.getBlockHash();
    //      * //System.out.println(firstParty.hashCode());
    //      * blockchain1.add(firstParty);
    //      * } else if (name.equals("BJP")) {
    //      * String[] SecondPartyValues = {adhaar, name};
    //      * Block secondParty = new Block(SecondPartyValues, secondPartyHash);
    //      * secondPartyHash = secondParty.getBlockHash();
    //      * //System.out.println(secondParty.hashCode());
    //      * blockchain2.add(secondParty);
    //      * } else {
    //      * System.out.println("bye");
    //      * break;
    //      * }
    //      * System.out.println("again..........");
    //      */
    //     // System.out.println("Do you want to exit ? (1/0)");
    //     int n = sc.nextInt();
    //     if (n == 1)
    //         break;

    //     /*
    //      * System.out.println(blockchain1.toString());
    //      * //System.out.println(blockchain2.toString());
    //      * 
    //      * System.out.println(blockchain1.size());
    //      * /*
    //      * if (blockchain1.size()>blockchain2.size())
    //      * {
    //      * System.out.println("TMC Won");
    //      * }
    //      * else
    //      * System.out.println("BJP Won");
    //      */
    // }

    // public static String declareResult(ArrayList<Block> blockchain1) {

    //     int prevHash = blockchain1.get(0).getBlockHash();

    //     int BJP = 0, CPM = 0, TMC = 0;

    //     for (int i = 1; i < blockchain1.size(); i++) {
    //         Block temp = blockchain1.get(i);
    //         int blockHash = Arrays.hashCode(new int[] { Arrays.hashCode(temp.getTransaction()), prevHash });

    //         if (blockHash != temp.getBlockHash())
    //             return "Alert! System has been tampered. Location " + i;

    //         String[] transaction = temp.getTransaction().clone();
    //         transaction[2] = "BJP";
    //         blockHash = Arrays.hashCode(new int[] { Arrays.hashCode(transaction), prevHash });
    //         if (blockHash == temp.getBlockHash()) {
    //             BJP++;
    //             prevHash = temp.getBlockHash();
    //             continue;
    //         }

    //         transaction[2] = "CPM";
    //         blockHash = Arrays.hashCode(new int[] { Arrays.hashCode(transaction), prevHash });
    //         if (blockHash == temp.getBlockHash()) {
    //             CPM++;
    //             prevHash = temp.getBlockHash();
    //             continue;
    //         }

    //         transaction[2] = "TMC";
    //         blockHash = Arrays.hashCode(new int[] { Arrays.hashCode(transaction), prevHash });
    //         if (blockHash == temp.getBlockHash()) {
    //             TMC++;
    //             prevHash = temp.getBlockHash();
    //             continue;
    //         }

    //     }

    //     if (BJP > CPM && BJP > TMC)
    //         return "BJP wins!";
    //     else if (TMC > CPM)
    //         return "TMC wins!";
    //     else
    //         return "CPM wins!";
    // }

    // // public static void main(String[] args)
    // // {
    // // ArrayList<Block> blockchain1 = new ArrayList<Block>();
    // // pollVotes(blockchain1);

    // // System.out.println(declareResult(blockchain1));

    // // }

}
