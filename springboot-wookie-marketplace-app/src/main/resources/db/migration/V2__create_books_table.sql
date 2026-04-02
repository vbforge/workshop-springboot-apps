-- Create books table
CREATE TABLE IF NOT EXISTS books (
                                     book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL UNIQUE,
                                     description TEXT NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     cover_image TEXT NOT NULL,
                                     price DECIMAL(10,2) NOT NULL,
                                     is_published BOOLEAN NOT NULL DEFAULT TRUE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
                                     INDEX idx_title (title),
                                     INDEX idx_user_id (user_id),
                                     INDEX idx_is_published (is_published),
                                     INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;