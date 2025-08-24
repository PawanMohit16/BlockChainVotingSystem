package net.codejava.controller;

import net.codejava.model.FaceData;
import net.codejava.service.FaceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/face")
@CrossOrigin(origins = "*")
public class FaceDataController {
    
    @Autowired
    private FaceDataService faceDataService;
    
    /**
     * Save face data
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveFaceData(@RequestBody FaceData faceData) {
        try {
            FaceData savedFaceData = faceDataService.saveFaceData(faceData);
            return ResponseEntity.ok(savedFaceData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving face data: " + e.getMessage());
        }
    }
    
    /**
     * Get face data by username
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getFaceDataByUsername(@PathVariable String username) {
        try {
            List<FaceData> faceDataList = faceDataService.getFaceDataByUsername(username);
            return ResponseEntity.ok(faceDataList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving face data: " + e.getMessage());
        }
    }
    
    /**
     * Get latest face data by username
     */
    @GetMapping("/user/{username}/latest")
    public ResponseEntity<?> getLatestFaceDataByUsername(@PathVariable String username) {
        try {
            Optional<FaceData> faceData = faceDataService.getLatestFaceDataByUsername(username);
            if (faceData.isPresent()) {
                return ResponseEntity.ok(faceData.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving face data: " + e.getMessage());
        }
    }
    
    /**
     * Check if user has face data
     */
    @GetMapping("/user/{username}/exists")
    public ResponseEntity<?> hasFaceData(@PathVariable String username) {
        try {
            boolean exists = faceDataService.hasFaceData(username);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking face data: " + e.getMessage());
        }
    }
    
    /**
     * Delete face data by username
     */
    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> deleteFaceDataByUsername(@PathVariable String username) {
        try {
            faceDataService.deleteFaceDataByUsername(username);
            return ResponseEntity.ok("Face data deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting face data: " + e.getMessage());
        }
    }
    
    /**
     * Get face data by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFaceDataById(@PathVariable String id) {
        try {
            Optional<FaceData> faceData = faceDataService.getFaceDataById(id);
            if (faceData.isPresent()) {
                return ResponseEntity.ok(faceData.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving face data: " + e.getMessage());
        }
    }
    
    /**
     * Update face data
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFaceData(@PathVariable String id, @RequestBody FaceData faceData) {
        try {
            faceData.setId(id);
            FaceData updatedFaceData = faceDataService.updateFaceData(faceData);
            return ResponseEntity.ok(updatedFaceData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating face data: " + e.getMessage());
        }
    }
    
    /**
     * Delete face data by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFaceDataById(@PathVariable String id) {
        try {
            faceDataService.deleteFaceDataById(id);
            return ResponseEntity.ok("Face data deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting face data: " + e.getMessage());
        }
    }
    
    /**
     * Get all face data
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllFaceData() {
        try {
            List<FaceData> allFaceData = faceDataService.getAllFaceData();
            return ResponseEntity.ok(allFaceData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving all face data: " + e.getMessage());
        }
    }
}
