package net.codejava.repository;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import net.codejava.model.Votedata;

public interface VoteRepo extends MongoRepository<Votedata, String> {

	@Query("{'username': ?0}")
	public Votedata findByUsername(String username);

	@Query(value = "{}", sort = "{'date': -1}")
	public Votedata findTopByOrderByDateDesc();

	// Note: MongoDB doesn't support native queries like SQL
	// These methods will need to be implemented differently
	// For now, we'll comment them out
	/*
	public void copyData(String username, String currhash, Date date, String prevhash);
	public void copyData();
	*/

	@Query(value = "{}", count = true)
	public Long findcount();

	public Votedata findByCurrhash(String currhash);

}
