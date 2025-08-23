package net.codejava.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.codejava.model.Block;
import net.codejava.model.Votedata;
import net.codejava.repository.VoteRepo;
import net.codejava.smartcontract.VoteSmartContract;

@Service
public class VoteService {
    private final AtomicBoolean isVotingActive = new AtomicBoolean(false);

    @Autowired
    private VoteRepo voterepo;

    @Autowired
    private VoteSmartContract smartcontract;
    
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
    }
    
    public Map<String, Object> getVotingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get total votes cast
        long totalVotes = voterepo.count();
        stats.put("totalVotes", totalVotes);
        
        // Initialize vote counts by candidate/choice
        Map<String, Long> votesByCandidate = new HashMap<>();
        List<Votedata> allVotes = voterepo.findAll();
        
        // Count votes by candidate/choice
        for (Votedata vote : allVotes) {
            String candidateVote = vote.getCandidate();
            if (candidateVote != null && !candidateVote.trim().isEmpty()) {
                votesByCandidate.put(candidateVote, 
                    votesByCandidate.getOrDefault(candidateVote, 0L) + 1);
            }
        }
        
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
        if (!isVotingActive()) {
            throw new IllegalStateException("Voting is not active at this time");
        }
        
        // Check if user has already voted
        if (userExists(adhhar)) {
            throw new IllegalStateException("You have already voted");
        }
        
        // Correct table if needed
        if (!smartcontract.checkTable()) {
            smartcontract.correctTableValues();
        }
        
        Votedata lastEntry = voterepo.findTopByOrderByDateDesc();
        System.out.println("Last vote entry: " + lastEntry);
        
        String[] data = { adhhar, name, candidateName };
        Block block;
        
        if (lastEntry == null) {
            // This is the first vote, use the genesis block hash
            System.out.println("First vote in the system");
            block = new Block(data, "0");
        } else {
            System.out.println(adhhar + "-----" + name + "-----" + candidateName + "-----" + lastEntry.getCurrhash());
            block = new Block(data, lastEntry.getCurrhash());
        }

        Votedata vote = new Votedata();
        vote.setUsername(adhhar);
        vote.setCandidate(candidateName); // Store the candidate/party that was voted for
        vote.setCurrhash(block.getBlockHash());
        vote.setPrevhash(block.getPreviousBlockHash());
        vote.setDate(new Date());
        System.out.println("*************** Vote Saved*********************");
        voterepo.save(vote);
        // Note: MongoDB doesn't support native SQL operations
        // This functionality would need to be implemented differently
        // voterepo.copyData(vote.getUsername(), vote.getCurrhash(), vote.getDate(), vote.getPrevhash());

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
