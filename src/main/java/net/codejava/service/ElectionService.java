package net.codejava.service;

import net.codejava.model.Election;
import net.codejava.repository.ElectionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ElectionService {
    
    @Autowired
    private ElectionRepo electionRepo;
    
    // Create new election
    public Election createElection(Election election) {
        election.setStatus(Election.ElectionStatus.PENDING);
        election.setCreatedAt(LocalDateTime.now());
        election.setUpdatedAt(LocalDateTime.now());
        return electionRepo.save(election);
    }
    
    // Get election by ID
    public Optional<Election> getElectionById(String id) {
        return electionRepo.findById(id);
    }
    
    // Get all elections
    public List<Election> getAllElections() {
        return electionRepo.findAll();
    }
    
    // Get elections by status
    public List<Election> getElectionsByStatus(Election.ElectionStatus status) {
        return electionRepo.findByStatus(status);
    }
    
    // Get active elections
    public List<Election> getActiveElections() {
        return electionRepo.findActiveElections(LocalDateTime.now());
    }
    
    // Update election
    public Election updateElection(Election election) {
        election.setUpdatedAt(LocalDateTime.now());
        return electionRepo.save(election);
    }
    
    // Delete election
    public void deleteElection(String id) {
        electionRepo.deleteById(id);
    }
    
    // Activate election
    public Election activateElection(String id) {
        Optional<Election> electionOpt = electionRepo.findById(id);
        if (electionOpt.isPresent()) {
            Election election = electionOpt.get();
            election.setStatus(Election.ElectionStatus.ACTIVE);
            election.setUpdatedAt(LocalDateTime.now());
            return electionRepo.save(election);
        }
        return null;
    }
    
    // Complete election
    public Election completeElection(String id) {
        Optional<Election> electionOpt = electionRepo.findById(id);
        if (electionOpt.isPresent()) {
            Election election = electionOpt.get();
            election.setStatus(Election.ElectionStatus.COMPLETED);
            election.setUpdatedAt(LocalDateTime.now());
            return electionRepo.save(election);
        }
        return null;
    }
    
    // Cancel election
    public Election cancelElection(String id) {
        Optional<Election> electionOpt = electionRepo.findById(id);
        if (electionOpt.isPresent()) {
            Election election = electionOpt.get();
            election.setStatus(Election.ElectionStatus.CANCELLED);
            election.setUpdatedAt(LocalDateTime.now());
            return electionRepo.save(election);
        }
        return null;
    }
    
    // Search elections by title
    public List<Election> searchElectionsByTitle(String title) {
        return electionRepo.findByTitleContainingIgnoreCase(title);
    }
    
    // Get election statistics
    public ElectionStats getElectionStats() {
        long totalElections = electionRepo.count();
        long pendingElections = electionRepo.countByStatus(Election.ElectionStatus.PENDING);
        long activeElections = electionRepo.countByStatus(Election.ElectionStatus.ACTIVE);
        long completedElections = electionRepo.countByStatus(Election.ElectionStatus.COMPLETED);
        long cancelledElections = electionRepo.countByStatus(Election.ElectionStatus.CANCELLED);
        
        return new ElectionStats(totalElections, pendingElections, activeElections, completedElections, cancelledElections);
    }
    
    // Scheduled task to automatically update election statuses
    @Scheduled(fixedRate = 60000) // Run every minute
    public void updateElectionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        
        // Activate elections that should start
        List<Election> electionsToActivate = electionRepo.findElectionsToActivate(now);
        for (Election election : electionsToActivate) {
            election.setStatus(Election.ElectionStatus.ACTIVE);
            election.setUpdatedAt(now);
            electionRepo.save(election);
        }
        
        // Complete elections that should end
        List<Election> electionsToComplete = electionRepo.findElectionsToComplete(now);
        for (Election election : electionsToComplete) {
            election.setStatus(Election.ElectionStatus.COMPLETED);
            election.setUpdatedAt(now);
            electionRepo.save(election);
        }
    }
    
    // Inner class for election statistics
    public static class ElectionStats {
        private final long totalElections;
        private final long pendingElections;
        private final long activeElections;
        private final long completedElections;
        private final long cancelledElections;
        
        public ElectionStats(long totalElections, long pendingElections, long activeElections, 
                           long completedElections, long cancelledElections) {
            this.totalElections = totalElections;
            this.pendingElections = pendingElections;
            this.activeElections = activeElections;
            this.completedElections = completedElections;
            this.cancelledElections = cancelledElections;
        }
        
        // Getters
        public long getTotalElections() { return totalElections; }
        public long getPendingElections() { return pendingElections; }
        public long getActiveElections() { return activeElections; }
        public long getCompletedElections() { return completedElections; }
        public long getCancelledElections() { return cancelledElections; }
    }
}
