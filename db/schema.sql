-- =====================================================
-- Online Voting System Database Schema
-- File: schema.sql
-- Description: Complete database setup for the voting system
-- =====================================================

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS collegeproject;
USE collegeproject;

-- =====================================================
-- USERS TABLE - Store registered user information
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    photos VARCHAR(255),
    birthday DATE,
    role VARCHAR(255) DEFAULT 'ROLE_USER',
    votestatus VARCHAR(10) DEFAULT '0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- CANDIDATES TABLE - Store candidate information
-- =====================================================
CREATE TABLE IF NOT EXISTS candidates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    party VARCHAR(255) NOT NULL UNIQUE,
    candidate_image_path VARCHAR(500),
    partypic VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- VOTEDATA TABLE - Store voting records with blockchain hashes
-- =====================================================
CREATE TABLE IF NOT EXISTS votedata (
    username VARCHAR(255) PRIMARY KEY,
    prevhash VARCHAR(255) NOT NULL,
    currhash VARCHAR(255) NOT NULL UNIQUE,
    date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- =====================================================
-- VOTECOPY TABLE - Backup table for blockchain operations
-- =====================================================
CREATE TABLE IF NOT EXISTS votecopy (
    username VARCHAR(255) PRIMARY KEY,
    prevhash VARCHAR(255) NOT NULL,
    currhash VARCHAR(255) NOT NULL UNIQUE,
    date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- PENDING TABLE - Store pending user registrations
-- =====================================================
CREATE TABLE IF NOT EXISTS pending (
    username VARCHAR(255) PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    photos VARCHAR(255),
    birthday DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- BLOCKS TABLE - Store blockchain blocks (optional enhancement)
-- =====================================================
CREATE TABLE IF NOT EXISTS blocks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    block_hash VARCHAR(255) UNIQUE NOT NULL,
    previous_hash VARCHAR(255),
    data TEXT NOT NULL,
    timestamp DATETIME NOT NULL,
    nonce INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- CREATE INDEXES FOR BETTER PERFORMANCE
-- =====================================================

-- Index on users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_votestatus ON users(votestatus);

-- Index on candidates table
CREATE INDEX idx_candidates_party ON candidates(party);

-- Index on votedata table
CREATE INDEX idx_votedata_currhash ON votedata(currhash);
CREATE INDEX idx_votedata_date ON votedata(date);

-- Index on blocks table
CREATE INDEX idx_blocks_hash ON blocks(block_hash);
CREATE INDEX idx_blocks_previous_hash ON blocks(previous_hash);

-- =====================================================
-- INSERT INITIAL DATA
-- =====================================================

-- Insert default admin user (password: admin123)
INSERT INTO users (username, firstname, lastname, email, password, role, votestatus) 
VALUES ('admin', 'Admin', 'User', 'admin@voting.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ROLE_ADMIN', '0')
ON DUPLICATE KEY UPDATE role = 'ROLE_ADMIN';

-- Insert sample candidates
INSERT INTO candidates (firstname, lastname, party, candidate_image_path, partypic) 
VALUES 
('Narendra', 'Modi', 'BJP', '/images/candidates/modi.jpg', '/images/parties/bjp.jpg'),
('Rahul', 'Gandhi', 'Congress', '/images/candidates/rahul.jpg', '/images/parties/congress.jpg'),
('Mamata', 'Banerjee', 'TMC', '/images/candidates/mamata.jpg', '/images/parties/tmc.jpg'),
('Arvind', 'Kejriwal', 'AAP', '/images/candidates/kejriwal.jpg', '/images/parties/aap.jpg')
ON DUPLICATE KEY UPDATE 
    firstname = VALUES(firstname),
    lastname = VALUES(lastname),
    candidate_image_path = VALUES(candidate_image_path),
    partypic = VALUES(partypic);

-- Insert genesis block for blockchain
INSERT INTO blocks (block_hash, previous_hash, data, timestamp, nonce) 
VALUES ('0000000000000000000000000000000000000000000000000000000000000000', NULL, 'Genesis Block', NOW(), 0)
ON DUPLICATE KEY UPDATE timestamp = NOW();

-- =====================================================
-- CREATE VIEWS FOR COMMON QUERIES
-- =====================================================

-- View for user voting status
CREATE OR REPLACE VIEW user_voting_status AS
SELECT 
    u.username,
    u.firstname,
    u.lastname,
    u.email,
    u.role,
    u.votestatus,
    CASE 
        WHEN v.username IS NOT NULL THEN 'VOTED'
        ELSE 'NOT_VOTED'
    END as voting_status,
    v.date as voted_date
FROM users u
LEFT JOIN votedata v ON u.username = v.username;

-- View for candidate vote counts
CREATE OR REPLACE VIEW candidate_vote_counts AS
SELECT 
    c.party,
    c.firstname,
    c.lastname,
    COUNT(v.username) as vote_count
FROM candidates c
LEFT JOIN votedata v ON v.currhash IN (
    SELECT currhash FROM votecopy
)
GROUP BY c.party, c.firstname, c.lastname
ORDER BY vote_count DESC;

-- =====================================================
-- CREATE STORED PROCEDURES
-- =====================================================

DELIMITER //

-- Procedure to start voting
CREATE PROCEDURE StartVoting()
BEGIN
    UPDATE users SET votestatus = '1' WHERE username = 'admin';
    SELECT 'Voting started successfully' as message;
END //

-- Procedure to stop voting
CREATE PROCEDURE StopVoting()
BEGIN
    UPDATE users SET votestatus = '2' WHERE username = 'admin';
    SELECT 'Voting stopped successfully' as message;
END //

-- Procedure to get voting statistics
CREATE PROCEDURE GetVotingStats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM users WHERE role = 'ROLE_USER') as total_users,
        (SELECT COUNT(*) FROM votedata) as total_votes,
        (SELECT COUNT(*) FROM candidates) as total_candidates,
        (SELECT votestatus FROM users WHERE username = 'admin') as voting_status;
END //

DELIMITER ;

-- =====================================================
-- GRANT PERMISSIONS (if needed)
-- =====================================================

-- Grant all privileges to root user on this database
-- GRANT ALL PRIVILEGES ON collegeproject.* TO 'root'@'localhost';

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify tables are created
SELECT 'Tables created successfully' as status;
SHOW TABLES;

-- Verify initial data
SELECT 'Users count:' as info, COUNT(*) as count FROM users;
SELECT 'Candidates count:' as info, COUNT(*) as count FROM candidates;
SELECT 'Admin user:' as info, username, role FROM users WHERE role = 'ROLE_ADMIN';

-- =====================================================
-- END OF SCHEMA FILE
-- =====================================================
