package net.codejava.repository;

import net.codejava.model.Election;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ElectionRepo extends MongoRepository<Election, String> {
    
    // Find elections by status
    List<Election> findByStatus(Election.ElectionStatus status);
    
    // Find active elections (current time is between start and end date)
    @Query("{'status': 'ACTIVE', 'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}}")
    List<Election> findActiveElections(LocalDateTime now);
    
    // Find elections by title (partial match)
    List<Election> findByTitleContainingIgnoreCase(String title);
    
    // Find elections starting after a specific date
    List<Election> findByStartDateAfter(LocalDateTime date);
    
    // Find elections ending before a specific date
    List<Election> findByEndDateBefore(LocalDateTime date);
    
    // Find elections within a date range
    @Query("{'startDate': {$gte: ?0}, 'endDate': {$lte: ?1}}")
    List<Election> findElectionsInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count elections by status
    long countByStatus(Election.ElectionStatus status);
    
    // Find elections that need to be activated (start date has passed but still pending)
    @Query("{'status': 'PENDING', 'startDate': {$lte: ?0}}")
    List<Election> findElectionsToActivate(LocalDateTime now);
    
    // Find elections that need to be completed (end date has passed but still active)
    @Query("{'status': 'ACTIVE', 'endDate': {$lte: ?0}}")
    List<Election> findElectionsToComplete(LocalDateTime now);
}
