package net.codejava.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "face_data")
public class FaceData {
    @Id
    private String id;
    
    private String username; // Reference to user
    private String faceImageBase64; // Base64 encoded face image
    private List<Double> faceEncoding; // Face recognition encoding
    private String imageType; // Image format (jpg, png, etc.)
    private long timestamp; // When the face data was stored
    
    public FaceData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public FaceData(String username, String faceImageBase64, List<Double> faceEncoding, String imageType) {
        this.username = username;
        this.faceImageBase64 = faceImageBase64;
        this.faceEncoding = faceEncoding;
        this.imageType = imageType;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
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
    
    public String getFaceImageBase64() {
        return faceImageBase64;
    }
    
    public void setFaceImageBase64(String faceImageBase64) {
        this.faceImageBase64 = faceImageBase64;
    }
    
    public List<Double> getFaceEncoding() {
        return faceEncoding;
    }
    
    public void setFaceEncoding(List<Double> faceEncoding) {
        this.faceEncoding = faceEncoding;
    }
    
    public String getImageType() {
        return imageType;
    }
    
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "FaceData{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", imageType='" + imageType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
