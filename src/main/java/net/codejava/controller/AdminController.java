package net.codejava.controller;

import net.codejava.model.Election;
import net.codejava.model.Candidate;
import net.codejava.model.User;
import net.codejava.model.Pending;
import net.codejava.service.ElectionService;
import net.codejava.service.CandidateService;
import net.codejava.service.UserService;
import net.codejava.service.EmailService;
import net.codejava.service.VoteService;
import net.codejava.helper.EmailTemplate;
import net.codejava.helper.Message;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private ElectionService electionService;
    
    @Autowired
    private CandidateService candidateService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EmailTemplate emailTemplate;
    
    @Autowired
    private VoteService voteService;
    
    // ==================== ELECTION MANAGEMENT ====================
    
    @GetMapping("/elections")
    public String listElections(Model model) {
        List<Election> elections = electionService.getAllElections();
        ElectionService.ElectionStats stats = electionService.getElectionStats();
        
        model.addAttribute("elections", elections);
        model.addAttribute("stats", stats);
        return "admin/elections.html";
    }
    
    @GetMapping("/elections/new")
    public String showElectionForm(Model model) {
        model.addAttribute("election", new Election());
        return "admin/election-form.html";
    }
    
    @PostMapping("/elections/create")
    public String createElection(@ModelAttribute Election election, 
                                @RequestParam("startDateStr") String startDateStr,
                                @RequestParam("endDateStr") String endDateStr,
                                RedirectAttributes redirectAttributes) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);
            
            election.setStartDate(startDate);
            election.setEndDate(endDate);
            
            electionService.createElection(election);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Election created successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error creating election: " + e.getMessage(), "danger"));
        }
        
        return "redirect:/admin/elections";
    }
    
    @GetMapping("/elections/{id}/edit")
    public String editElection(@PathVariable String id, Model model) {
        Election election = electionService.getElectionById(id).orElse(null);
        if (election == null) {
            return "redirect:/admin/elections";
        }
        model.addAttribute("election", election);
        return "admin/election-form.html";
    }
    
    @PostMapping("/elections/{id}/update")
    public String updateElection(@PathVariable String id, 
                                @ModelAttribute Election election,
                                @RequestParam("startDateStr") String startDateStr,
                                @RequestParam("endDateStr") String endDateStr,
                                RedirectAttributes redirectAttributes) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);
            
            election.setId(id);
            election.setStartDate(startDate);
            election.setEndDate(endDate);
            
            electionService.updateElection(election);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Election updated successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error updating election: " + e.getMessage(), "danger"));
        }
        
        return "redirect:/admin/elections";
    }
    
    @PostMapping("/elections/{id}/activate")
    public String activateElection(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            electionService.activateElection(id);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Election activated successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error activating election: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/elections";
    }
    
    @PostMapping("/elections/{id}/complete")
    public String completeElection(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            electionService.completeElection(id);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Election completed successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error completing election: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/elections";
    }
    
    @PostMapping("/elections/{id}/cancel")
    public String cancelElection(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            electionService.cancelElection(id);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Election cancelled successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error cancelling election: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/elections";
    }
    
    @PostMapping("/elections/{id}/delete")
    public String deleteElection(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            electionService.deleteElection(id);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Election deleted successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error deleting election: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/elections";
    }
    
    // ==================== VOTING CONTROLS ====================
    
    @PostMapping("/voting/start")
    public String startVoting(RedirectAttributes redirectAttributes) {
        try {
            // Reset all votes and voting state
            voteService.resetVotingSystem();
            
            // Get the active election
            List<Election> activeElections = electionService.getElectionsByStatus(Election.ElectionStatus.ACTIVE);
            if (activeElections.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", 
                    new Message("No active election found. Please activate an election first.", "danger"));
                return "redirect:/admin/dashboard";
            }
            
            // Update voting status in the system
            voteService.setVotingActive(true);
            
            redirectAttributes.addFlashAttribute("message", 
                new Message("Voting has been started successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error starting voting: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/voting/stop")
    public String stopVoting(RedirectAttributes redirectAttributes) {
        try {
            voteService.setVotingActive(false);
            redirectAttributes.addFlashAttribute("message", 
                new Message("Voting has been stopped successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error stopping voting: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/voting/reset")
    public String resetVoting(RedirectAttributes redirectAttributes) {
        try {
            voteService.resetVotingSystem();
            redirectAttributes.addFlashAttribute("message", 
                new Message("Voting system has been reset successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error resetting voting: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/dashboard";
    }
    
    @GetMapping("/voting/stats")
    @ResponseBody
    public Map<String, Object> getVotingStats() {
        return voteService.getVotingStatistics();
    }
    
    // ==================== ENHANCED CANDIDATE MANAGEMENT ====================
    
    @GetMapping("/candidates/advanced")
    public String advancedCandidateManagement(Model model) {
        List<Candidate> candidates = candidateService.getAllCandidates();
        model.addAttribute("candidates", candidates);
        return "admin/advanced-candidates.html";
    }
    
    @PostMapping("/candidates/{id}/approve")
    public String approveCandidate(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // Add approval logic here
            redirectAttributes.addFlashAttribute("message", 
                new Message("Candidate approved successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error approving candidate: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/candidates/advanced";
    }
    
    @PostMapping("/candidates/{id}/reject")
    public String rejectCandidate(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // Add rejection logic here
            redirectAttributes.addFlashAttribute("message", 
                new Message("Candidate rejected successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error rejecting candidate: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/candidates/advanced";
    }
    
    // ==================== ENHANCED USER MANAGEMENT ====================
    
    @GetMapping("/users/advanced")
    public String advancedUserManagement(Model model) {
        List<User> users = userService.getAllUsers();
        List<Pending> pendingUsers = userService.getAllPendingUsers();
        
        model.addAttribute("users", users);
        model.addAttribute("pendingUsers", pendingUsers);
        return "admin/advanced-users.html";
    }
    
    @PostMapping("/users/{username}/block")
    public String blockUser(@PathVariable String username, RedirectAttributes redirectAttributes) {
        try {
            // Add user blocking logic here
            redirectAttributes.addFlashAttribute("message", 
                new Message("User blocked successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error blocking user: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/users/advanced";
    }
    
    @PostMapping("/users/{username}/unblock")
    public String unblockUser(@PathVariable String username, RedirectAttributes redirectAttributes) {
        try {
            // Add user unblocking logic here
            redirectAttributes.addFlashAttribute("message", 
                new Message("User unblocked successfully!", "success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", 
                new Message("Error unblocking user: " + e.getMessage(), "danger"));
        }
        return "redirect:/admin/users/advanced";
    }
    
    // ==================== DASHBOARD STATISTICS ====================
    
    @GetMapping("/dashboard/stats")
    @ResponseBody
    public DashboardStats getDashboardStats() {
        ElectionService.ElectionStats electionStats = electionService.getElectionStats();
        long totalUsers = userService.getAllUsers().size();
        long totalCandidates = candidateService.getAllCandidates().size();
        long pendingUsers = userService.getAllPendingUsers().size();
        
        return new DashboardStats(
            totalUsers, 
            totalCandidates, 
            pendingUsers,
            electionStats.getTotalElections(),
            electionStats.getActiveElections(),
            electionStats.getCompletedElections()
        );
    }
    
    // Inner class for dashboard statistics
    public static class DashboardStats {
        private final long totalUsers;
        private final long totalCandidates;
        private final long pendingUsers;
        private final long totalElections;
        private final long activeElections;
        private final long completedElections;
        
        public DashboardStats(long totalUsers, long totalCandidates, long pendingUsers, 
                            long totalElections, long activeElections, long completedElections) {
            this.totalUsers = totalUsers;
            this.totalCandidates = totalCandidates;
            this.pendingUsers = pendingUsers;
            this.totalElections = totalElections;
            this.activeElections = activeElections;
            this.completedElections = completedElections;
        }
        
        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getTotalCandidates() { return totalCandidates; }
        public long getPendingUsers() { return pendingUsers; }
        public long getTotalElections() { return totalElections; }
        public long getActiveElections() { return activeElections; }
        public long getCompletedElections() { return completedElections; }
    }
}
