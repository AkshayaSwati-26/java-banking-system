-- schema.sql
CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

CREATE TABLE accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100),
    pin VARCHAR(10),
    balance DOUBLE DEFAULT 0,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT,
    type VARCHAR(20),
    amount DOUBLE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    related_account VARCHAR(20),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE fixed_deposits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT,
    principal DOUBLE,
    months INT,
    interest_rate DOUBLE,
    maturity_amount DOUBLE,
    start_date DATE,
    maturity_date DATE,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
