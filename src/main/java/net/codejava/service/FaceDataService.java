package net.codejava.service;

import net.codejava.model.FaceData;
import net.codejava.repository.FaceDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FaceDataService {
    
    @Autowired
    private FaceDataRepo faceDataRepo;
    
    /**
     * Save face data to MongoDB
     */
    public FaceData saveFaceData(FaceData faceData) {
        return faceDataRepo.save(faceData);
    }
    
    /**
     * Save face data with all parameters
     */
    public FaceData saveFaceData(String username, String faceImageBase64, List<Double> faceEncoding, String imageType) {
        FaceData faceData = new FaceData(username, faceImageBase64, faceEncoding, imageType);
        return faceDataRepo.save(faceData);
    }
    
    /**
     * Get all face data for a user
     */
    public List<FaceData> getFaceDataByUsername(String username) {
        return faceDataRepo.findByUsername(username);
    }
    
    /**
     * Get the most recent face data for a user
     */
    public Optional<FaceData> getLatestFaceDataByUsername(String username) {
        return faceDataRepo.findTopByUsernameOrderByTimestampDesc(username);
    }
    
    /**
     * Get face data by username and image type
     */
    public List<FaceData> getFaceDataByUsernameAndImageType(String username, String imageType) {
        return faceDataRepo.findByUsernameAndImageType(username, imageType);
    }
    
    /**
     * Delete all face data for a user
     */
    public void deleteFaceDataByUsername(String username) {
        faceDataRepo.deleteByUsername(username);
    }
    
    /**
     * Check if user has face data
     */
    public boolean hasFaceData(String username) {
        return faceDataRepo.existsByUsername(username);
    }
    
    /**
     * Get face data by ID
     */
    public Optional<FaceData> getFaceDataById(String id) {
        return faceDataRepo.findById(id);
    }
    
    /**
     * Update face data
     */
    public FaceData updateFaceData(FaceData faceData) {
        if (faceData.getId() != null && faceDataRepo.existsById(faceData.getId())) {
            return faceDataRepo.save(faceData);
        }
        throw new IllegalArgumentException("Face data not found or ID is null");
    }
    
    /**
     * Delete face data by ID
     */
    public void deleteFaceDataById(String id) {
        faceDataRepo.deleteById(id);
    }
    
    /**
     * Get all face data
     */
    public List<FaceData> getAllFaceData() {
        return faceDataRepo.findAll();
    }
}
