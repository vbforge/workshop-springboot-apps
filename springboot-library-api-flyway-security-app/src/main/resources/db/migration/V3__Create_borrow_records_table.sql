CREATE TABLE borrow_records (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                book_id BIGINT NOT NULL,
                                borrow_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                due_date TIMESTAMP NOT NULL,
                                return_date TIMESTAMP,
                                late_fee DECIMAL(10, 2),
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);