package net.codejava.model;

import java.util.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "votedata")
public class Votedata {

    @Id
    private String id;
    
    private String username;
    private String candidate;
    private String prevhash;
    private String currhash;
    private Date date;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrevhash() {
        return prevhash;
    }

    public void setPrevhash(String prevhash) {
        this.prevhash = prevhash;
    }

    public String getCurrhash() {
        return currhash;
    }

    public void setCurrhash(String currhash) {
        this.currhash = currhash;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    @Override
    public String toString() {
        return "Votedata [id=" + id + ", username=" + username + ", candidate=" + candidate + ", currhash=" + currhash + ", prevhash=" + prevhash + ", date=" + date + "]";
    }

}