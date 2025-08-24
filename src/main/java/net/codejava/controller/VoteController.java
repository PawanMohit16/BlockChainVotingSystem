package net.codejava.controller;

import org.springframework.web.bind.annotation.ResponseBody;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.*;

import net.codejava.helper.EmailTemplate;
import net.codejava.helper.Message;
import net.codejava.model.Candidate;
import net.codejava.model.Votedata;
import net.codejava.model.User;
import net.codejava.repository.CandidateRepo;
import net.codejava.service.BlockchainService;
import net.codejava.repository.VoteRepo;
import net.codejava.service.CandidateService;
import net.codejava.service.EmailService;
import net.codejava.service.UserService;
import net.codejava.service.VoteService;
import net.codejava.smartcontract.VoteSmartContract;

// import net.codejava.service.BlockChain;

@Controller
@RequestMapping("/vote")
public class VoteController {
    @Autowired
    private VoteService voteService;

    @Autowired
    VoteSmartContract smartcontract;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private VoteRepo voteRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    EmailService emailservice;

    @Autowired
    EmailTemplate emailTemplate;

    @Autowired
    CandidateRepo candidaterepo;

    /**
     * Endpoint to verify face using static_vs_live_verification Python function
     */
    @GetMapping("/verify-face/{userId}")
    @ResponseBody
    public String verifyFace(@PathVariable("userId") String userId) {
        String pythonScript = "src/main/java/net/codejava/controller/main.py";
        ProcessBuilder pb = new ProcessBuilder("python", pythonScript, "--static-verify", userId);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            boolean isMatch = false;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (line.contains("MATCH: You are the same person.")) {
                    isMatch = true;
                }
                if (line.contains("NO MATCH: You are NOT the same person.")) {
                    isMatch = false;
                }
            }
            process.waitFor();
            if (isMatch) {
                return "success";
            } else {
                return "fail";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/votepage")
    public String vote(Principal principal, HttpSession session, Model model) {
        List<Candidate> candidates = candidateService.getAllCandidates();
        model.addAttribute("candidates", candidates);
        return "vote.html";
    }

    @GetMapping("/votecasted/{choice}")
    public String getParty(@PathVariable("choice") String choice, Principal principal, HttpSession session) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (principal == null || principal.getName() == null) {
            session.setAttribute("status", new Message("You must be logged in to vote.", "danger"));
            return "redirect:/login";
        }

        String name = principal.getName();

        try {
            // Check if user has already voted
            if (voteService.userExists(name)) {
                session.setAttribute("status", new Message("You have already voted. Thanks!", "warning"));
                return "redirect:/public/home";
            }

            // Get user details
            User user = userService.getUser(name);
            if (user == null) {
                session.setAttribute("status", new Message("User not found!", "danger"));
                return "redirect:/public/home";
            }

            // Process the vote
            boolean voteSuccess = voteService.isSuccessfull(choice, user.getUsername(), user.getFirstname());

            if (voteSuccess) {
                // Get the vote hash
                Votedata vote = voteRepo.findByUsername(name);
                if (vote != null) {
                    String hash = vote.getCurrhash();

                    try {
                        // Send confirmation email
                        String f = "Vote Successfully Recorded";
                        String s = "Your vote has been successfully recorded. Your unique vote hash (save this for verification): ";
                        String t = hash;
                        String email = user.getEmail();
                        String subject = "Your Vote Has Been Recorded";
                        String message = emailTemplate.getTemplate(f, s, t);

                        // Send email in a separate thread to avoid blocking
                        new Thread(() -> {
                            try {
                                System.out.println("[Vote Confirmation] Sending email to: " + email);
                                System.out.println("[Vote Confirmation] Subject: " + subject);
                                System.out.println("[Vote Confirmation] Message: " + message);
                                boolean emailSent = emailservice.sendEmail(subject, message, email);
                                System.out.println("[Vote Confirmation] Email sent result: " + emailSent);
                            } catch (Exception e) {
                                System.err.println("[Vote Confirmation] Failed to send confirmation email: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }).start();
                        // Update user's voted status
                        user.setVoted(true);
                        User updatedUser = userService.updateUser(user);

                        if (updatedUser != null) {
                            session.setAttribute("status", new Message("Thank you for voting! A confirmation has been sent to your email.", "success"));
                        } else {
                            session.setAttribute("status", new Message("Your vote was recorded, but we couldn't update your profile. Please contact support.", "warning"));
                        }
                    } catch (Exception e) {
                        System.err.println("Error in vote processing: " + e.getMessage());
                        session.setAttribute("status", new Message("Your vote was recorded, but we encountered an issue sending your confirmation.", "warning"));
                    }
                } else {
                    session.setAttribute("status", new Message("Vote recorded, but we couldn't generate a verification hash. Please contact support.", "warning"));
                }
            } else {
                session.setAttribute("status", new Message("Failed to process your vote. Please try again or contact support if the problem persists.", "danger"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("status", new Message("An unexpected error occurred while processing your vote. Our team has been notified.", "danger"));
        }

        return "redirect:/public/home";
    }

    @GetMapping("/showResults")
    public String getResults(Model model) throws NoSuchAlgorithmException {
        try {
            // Get the winning party from the smart contract
            String winningParty = smartcontract.voteCount();
            
            if (winningParty == null || winningParty.isEmpty()) {
                model.addAttribute("error", "No votes have been cast yet.");
                return "result.html";
            }

            System.out.println("-------------Winning Party-------------");
            System.out.println(winningParty);

            // Get the winning candidate details
            Candidate winner = candidaterepo.findByParty(winningParty);
            if (winner == null) {
                model.addAttribute("error", "Could not find details for the winning party: " + winningParty);
                return "result.html";
            }

            // Get all candidates and find the one with most votes
            List<Candidate> allCandidates = candidateService.getAllCandidates();
            
            // Find candidate with highest votes
            Candidate highestVoted = null;
            int maxVotes = -1;
            long totalVotes = 0;
            
            for (Candidate c : allCandidates) {
                totalVotes += c.getVoteCount();
                if (c.getVoteCount() > maxVotes) {
                    maxVotes = c.getVoteCount();
                    highestVoted = c;
                }
            }
            
            // If we have a vote leader from database, use that instead of blockchain result
            if (highestVoted != null && highestVoted.getVoteCount() > 0) {
                winner = highestVoted;
            }
            
            // Add attributes to the model
            model.addAttribute("winningParty", winner);
            model.addAttribute("allCandidates", allCandidates);
            model.addAttribute("totalVotes", totalVotes);
            model.addAttribute("votingEnded", true); // Indicate voting has ended
            
            // Log the results for debugging
            System.out.println("Total votes cast: " + totalVotes);
            System.out.println("Winning party: " + winner.getParty() + " - " + winner.getFirstname() + " " + winner.getLastname());
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while calculating results: " + e.getMessage());
        }

        return "result.html";
    }

    /**
     * Endpoint to show final vote confirmation page
     */
    @RequestMapping(value = "/final", method = RequestMethod.GET)
    public String finalConfirmation() {
        return "final.html";
    }
}
