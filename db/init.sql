-- =====================================================
-- Simple Database Initialization Script
-- File: init.sql
-- Use this for basic setup
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS collegeproject;
USE collegeproject;

-- Create basic tables
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    photos VARCHAR(255),
    birthday DATE,
    role VARCHAR(255) DEFAULT 'ROLE_USER',
    votestatus VARCHAR(10) DEFAULT '0'
);

CREATE TABLE IF NOT EXISTS candidates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    party VARCHAR(255) NOT NULL UNIQUE,
    candidate_image_path VARCHAR(500),
    partypic VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS votedata (
    username VARCHAR(255) PRIMARY KEY,
    prevhash VARCHAR(255) NOT NULL,
    currhash VARCHAR(255) NOT NULL UNIQUE,
    date DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS votecopy (
    username VARCHAR(255) PRIMARY KEY,
    prevhash VARCHAR(255) NOT NULL,
    currhash VARCHAR(255) NOT NULL UNIQUE,
    date DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS pending (
    username VARCHAR(255) PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    photos VARCHAR(255),
    birthday DATE
);

-- Insert admin user (password: admin123)
INSERT INTO users (username, firstname, lastname, email, password, role, votestatus) 
VALUES ('admin', 'Admin', 'User', 'admin@voting.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ROLE_ADMIN', '0')
ON DUPLICATE KEY UPDATE role = 'ROLE_ADMIN';

-- Insert sample candidates
INSERT INTO candidates (firstname, lastname, party, candidate_image_path, partypic) 
VALUES 
('Narendra', 'Modi', 'BJP', '/images/candidates/modi.jpg', '/images/parties/bjp.jpg'),
('Rahul', 'Gandhi', 'Congress', '/images/candidates/rahul.jpg', '/images/parties/congress.jpg')
ON DUPLICATE KEY UPDATE 
    firstname = VALUES(firstname),
    lastname = VALUES(lastname);

-- Show created tables
SHOW TABLES;
