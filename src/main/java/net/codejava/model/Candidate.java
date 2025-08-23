package net.codejava.model;

import java.beans.Transient;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Candidate{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String username;
    
    private String firstname;
    private String lastname;
  
    private String party;

    private String partypic;
    private String candidatepic;
    
    // Additional fields for candidate images
    private String candidate_image_path;

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
   
    
    public String getParty() {
        return party;
    }
    public void setParty(String party) {
        this.party = party;
    }
    public String getPartypic() {
        return partypic;
    }
    public void setPartypic(String partypic) {
        this.partypic = partypic;
    }
    public String getCandiatepic() {
        return candidatepic;
    }
    public void setCandidatepic(String canidatepic) {
        this.candidatepic = canidatepic;
    }
    
    public String getCandidateImagePath() {
        return candidate_image_path;
    }
    
    public void setCandidateImagePath(String candidate_image_path) {
        this.candidate_image_path = candidate_image_path;
    }
    
    @Transient
    public String getCandidatePicImagePath() {
        if (candidatepic == null || username == null)
            return null;

        return candidatepic;
    }
    
   
    
}