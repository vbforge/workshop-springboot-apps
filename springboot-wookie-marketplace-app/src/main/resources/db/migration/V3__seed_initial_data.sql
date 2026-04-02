-- Insert SUPER_ADMIN user (Lohgarra)
-- Password: defaultPassword (will be encoded by application)
INSERT INTO users (author_pseudonym, author_password, role, is_active)
VALUES ('Lohgarra', '$2a$10$NkM2cLQqXrY5wGqZxQyRzOeFgHjKlQwErTyUiOpAsDfGhJkLzXcVb', 'SUPER_ADMIN', TRUE)
ON DUPLICATE KEY UPDATE role = 'SUPER_ADMIN';

-- Insert a sample user for testing
INSERT INTO users (author_pseudonym, author_password, role, is_active)
VALUES ('TestAuthor', '$2a$10$NkM2cLQqXrY5wGqZxQyRzOeFgHjKlQwErTyUiOpAsDfGhJkLzXcVb', 'USER', TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Insert a sample book
INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'The Adventures of a Wookie', 'An exciting tale of adventure and friendship in the forests of Kashyyyk',
       user_id, 'https://example.com/cover1.jpg', 19.99, TRUE
FROM users WHERE author_pseudonym = 'TestAuthor'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;