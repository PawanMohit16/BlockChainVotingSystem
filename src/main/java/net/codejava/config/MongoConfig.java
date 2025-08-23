package net.codejava.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.codejava.model.User;
import net.codejava.model.Candidate;
import net.codejava.repository.UserRepo;
import net.codejava.repository.CandidateRepo;

import java.sql.Date;

@Configuration
public class MongoConfig {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CandidateRepo candidateRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            try {
                // Initialize admin user if not exists
                if (userRepo.findByUsername("admin") == null) {
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setFirstname("Admin");
                    admin.setLastname("User");
                    admin.setEmail("admin@voting.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole("ROLE_ADMIN");
                    admin.setVotestatus("0");
                    userRepo.save(admin);
                    System.out.println("Admin user created successfully");
                }

                // Initialize sample candidates if not exists
                if (candidateRepo.findByParty("BJP") == null) {
                    Candidate candidate1 = new Candidate();
                    candidate1.setFirstname("Narendra");
                    candidate1.setLastname("Modi");
                    candidate1.setParty("BJP");
                    candidate1.setCandidateImagePath("/images/candidates/modi.jpg");
                    candidate1.setPartypic("/images/parties/bjp.jpg");
                    candidateRepo.save(candidate1);
                    System.out.println("Candidate Narendra Modi created successfully");
                }

                if (candidateRepo.findByParty("Congress") == null) {
                    Candidate candidate2 = new Candidate();
                    candidate2.setFirstname("Rahul");
                    candidate2.setLastname("Gandhi");
                    candidate2.setParty("Congress");
                    candidate2.setCandidateImagePath("/images/candidates/rahul.jpg");
                    candidate2.setPartypic("/images/parties/congress.jpg");
                    candidateRepo.save(candidate2);
                    System.out.println("Candidate Rahul Gandhi created successfully");
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not initialize sample data due to MongoDB connection issues: " + e.getMessage());
                System.out.println("The application will continue to run, but you may need to manually create users and candidates.");
            }
        };
    }
}
