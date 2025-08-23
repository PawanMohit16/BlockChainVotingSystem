package net.codejava.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import net.codejava.model.Candidate;


@Repository
public interface CandidateRepo extends MongoRepository<Candidate, String> {

    public Candidate findByFirstname(String firstname);

    public Candidate findByParty(String partyname);
} 