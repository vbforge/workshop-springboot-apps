-- Insert a sample book
INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'The Adventures of a Wookie 2', 'Continue an exciting tale of adventure and friendship in the forests of Kashyyyk',
       user_id, 'https://example.com/cover1.jpg', 17.99, TRUE
FROM users WHERE author_pseudonym = 'TestAuthor'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'Wookie Wonderland', 'Adventures in the forests of Kashyyyk',
       user_id, 'https://example.com/cover.jpg', 10.99, TRUE
FROM users WHERE author_pseudonym = 'wookie_writer'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'Wookie Wonderland 2', 'Adventures in the forests of Kashyyyk',
       user_id, 'https://example.com/cover.jpg', 11.50, TRUE
FROM users WHERE author_pseudonym = 'wookie_writer'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'Wookie Story 1', 'Stories in Wookie',
       user_id, 'https://example.com/cover.jpg', 10.50, TRUE
FROM users WHERE author_pseudonym = 'Lohgarra'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'Wookie Story 2', 'Stories in Wookie',
       user_id, 'https://example.com/cover.jpg', 11.50, TRUE
FROM users WHERE author_pseudonym = 'Lohgarra'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'Wookie Story 3', 'Stories in Wookie',
       user_id, 'https://example.com/cover.jpg', 12.50, TRUE
FROM users WHERE author_pseudonym = 'Lohgarra'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'My Book 1', 'Book 1',
       user_id, 'https://example.com/cover.jpg', 7.50, TRUE
FROM users WHERE author_pseudonym = 'user2'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO books (title, description, user_id, cover_image, price, is_published)
SELECT 'My Book 2', 'Book 2',
       user_id, 'https://example.com/cover.jpg', 8.55, TRUE
FROM users WHERE author_pseudonym = 'user2'
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;