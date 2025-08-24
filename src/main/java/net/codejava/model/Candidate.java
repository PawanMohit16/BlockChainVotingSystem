package net.codejava.model;

import java.beans.Transient;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "candidates")
public class Candidate{

    @Id
    private String id;
    
    private String username;
    
    private String firstname;
    private String lastname;
  
    private String party;

    private String partypic;
    private String candidatepic;
    
    // Additional fields for candidate images
    private String candidate_image_path;

    private int voteCount = 0;

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public void incrementVoteCount() {
        this.voteCount++;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
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