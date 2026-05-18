-- ====================================
-- Book Management API - Database Schema
-- ====================================

DROP DATABASE IF EXISTS book_management_db;
CREATE DATABASE book_management_db;
USE book_management_db;

-- ====================================
-- Table: authors
-- ====================================
CREATE TABLE authors (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(255) NOT NULL,
                         biography TEXT,
                         date_of_birth DATE,
                         nationality VARCHAR(100),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         INDEX idx_author_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ====================================
-- Table: categories
-- ====================================
CREATE TABLE categories (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            INDEX idx_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ====================================
-- Table: publishers
-- ====================================
CREATE TABLE publishers (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(255) NOT NULL,
                            address VARCHAR(500),
                            website VARCHAR(255),
                            contact_email VARCHAR(255),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            INDEX idx_publisher_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ====================================
-- Table: books
-- ====================================
CREATE TABLE books (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       isbn VARCHAR(20) NOT NULL UNIQUE,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       publication_date DATE,
                       price DECIMAL(10, 2) NOT NULL,
                       stock_quantity INT NOT NULL DEFAULT 0,
                       language VARCHAR(50),
                       page_count INT,
                       category_id BIGINT,
                       publisher_id BIGINT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                       FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE SET NULL,
                       INDEX idx_book_isbn (isbn),
                       INDEX idx_book_title (title),
                       INDEX idx_book_category (category_id),
                       INDEX idx_book_publisher (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ====================================
-- Table: book_authors (Many-to-Many)
-- ====================================
CREATE TABLE book_authors (
                              book_id BIGINT NOT NULL,
                              author_id BIGINT NOT NULL,
                              PRIMARY KEY (book_id, author_id),
                              FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                              FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE,
                              INDEX idx_book (book_id),
                              INDEX idx_author (author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;