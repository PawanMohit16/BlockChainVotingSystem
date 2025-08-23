package net.codejava.repository;

import net.codejava.model.Election;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ElectionRepo extends JpaRepository<Election, Long> {
    
    // Find elections by status
    List<Election> findByStatus(Election.ElectionStatus status);
    
    // Find active elections (current time is between start and end date)
    @Query("SELECT e FROM Election e WHERE e.status = 'ACTIVE' AND :now BETWEEN e.startDate AND e.endDate")
    List<Election> findActiveElections(@Param("now") LocalDateTime now);
    
    // Find elections by title (partial match)
    List<Election> findByTitleContainingIgnoreCase(String title);
    
    // Find elections starting after a specific date
    List<Election> findByStartDateAfter(LocalDateTime date);
    
    // Find elections ending before a specific date
    List<Election> findByEndDateBefore(LocalDateTime date);
    
    // Find elections within a date range
    @Query("SELECT e FROM Election e WHERE e.startDate >= :startDate AND e.endDate <= :endDate")
    List<Election> findElectionsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    // Count elections by status
    long countByStatus(Election.ElectionStatus status);
    
    // Find elections that need to be activated (start date has passed but still pending)
    @Query("SELECT e FROM Election e WHERE e.status = 'PENDING' AND e.startDate <= :now")
    List<Election> findElectionsToActivate(@Param("now") LocalDateTime now);
    
    // Find elections that need to be completed (end date has passed but still active)
    @Query("SELECT e FROM Election e WHERE e.status = 'ACTIVE' AND e.endDate <= :now")
    List<Election> findElectionsToComplete(@Param("now") LocalDateTime now);
}
