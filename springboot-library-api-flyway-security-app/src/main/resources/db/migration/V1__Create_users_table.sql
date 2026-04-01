CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       role VARCHAR(20) NOT NULL,
                       signup_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       login_date TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);