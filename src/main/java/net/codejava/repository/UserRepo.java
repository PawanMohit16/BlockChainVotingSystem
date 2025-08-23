package net.codejava.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import net.codejava.model.User;

public interface UserRepo extends MongoRepository<User, String>{
	
	@Query("{'username': ?0}")
	public User findByUsername(String username);

	@Query("delete from User e where e.username =:username and fileName =:fileName")
	public void deleteEmployeeWithFile(String username, String fileName);

	@Query(value = "{}", count = true)
	public Long findUserCount();

}
