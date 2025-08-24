package net.codejava.repository;

import net.codejava.model.FaceData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FaceDataRepo extends MongoRepository<FaceData, String> {
    
    // Find face data by username
    List<FaceData> findByUsername(String username);
    
    // Find the most recent face data for a user
    @Query(value = "{'username': ?0}", sort = "{'timestamp': -1}")
    Optional<FaceData> findTopByUsernameOrderByTimestampDesc(String username);
    
    // Find face data by username and image type
    List<FaceData> findByUsernameAndImageType(String username, String imageType);
    
    // Delete all face data for a specific user
    void deleteByUsername(String username);
    
    // Check if user has face data
    boolean existsByUsername(String username);
}
