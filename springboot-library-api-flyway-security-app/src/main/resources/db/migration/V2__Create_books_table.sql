CREATE TABLE books (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(100) NOT NULL,
                       isbn VARCHAR(13) NOT NULL UNIQUE,
                       total_copies INT NOT NULL,
                       available_copies INT NOT NULL,
                       CONSTRAINT chk_copies CHECK (available_copies <= total_copies)
);