package net.codejava.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import net.codejava.model.Pending;

public interface PendingRepo extends MongoRepository<Pending, String>{
	
    
	@Query("{'username': ?0}")
	public Pending findByUsername(String username);
	/*
	@Query("delete from User e where e.username =:username and fileName =:fileName")
	public void deleteEmployeeWithFile(String username, String fileName);
	*/
	
	@Query(value = "{}", count = true)
	public Long findPendingCount();

}
