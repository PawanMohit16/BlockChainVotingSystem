package net.codejava.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "elections")
public class Election {
    
    @Id
    private String id;
    
    private String title;
    
    private String description;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private ElectionStatus status = ElectionStatus.PENDING;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Enum for election status
    public enum ElectionStatus {
        PENDING, ACTIVE, COMPLETED, CANCELLED
    }
    
    // Default constructor
    public Election() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public Election(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this();
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public ElectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ElectionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == ElectionStatus.ACTIVE && 
               now.isAfter(startDate) && 
               now.isBefore(endDate);
    }
    
    public boolean isPending() {
        return status == ElectionStatus.PENDING;
    }
    
    public boolean isCompleted() {
        return status == ElectionStatus.COMPLETED;
    }
    
    // @PreUpdate
    // public void preUpdate() {
    //     this.updatedAt = LocalDateTime.now();
    // }
}
