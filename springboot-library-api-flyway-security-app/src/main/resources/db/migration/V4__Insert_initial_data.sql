-- Insert a default users
INSERT INTO users (username, password, email, role, signup_date, login_date)
VALUES ('Bob', '$2a$10$7JGAZgogOdXRzZ7x7.A9hO4lihqCHHqzCsDSq1qC/r3kGMduvvTbq', 'bob@example.com', 'USER', NOW(), NOW());

INSERT INTO users (username, password, email, role, signup_date, login_date)
VALUES ('Dana', '$2a$10$w9L0yGUHVtxZPIYxpWDbZuJOohiT0dYzwmPkbgj4/4fyP0Gr7xASi', 'dana@example.com', 'USER', NOW(), NOW());

INSERT INTO users (username, password, email, role, signup_date, login_date)
VALUES ('John', '$2a$10$9mOKcXxgoC5f6omThopyjOVtKLlaKK4sJ1yIh3M044WoV7oIWoLpW', 'john@example.com', 'USER', NOW(), NOW());

-- Insert sample books
INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 5, 5);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('1984', 'George Orwell', '9780451524935', 8, 8);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('To Kill a Mockingbird', 'Harper Lee', '9780061120084', 6, 6);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('Pride and Prejudice', 'Jane Austen', '9780141439518', 7, 7);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('The Catcher in the Rye', 'J.D. Salinger', '9780316769488', 5, 5);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('Moby-Dick', 'Herman Melville', '9781503280786', 4, 4);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('War and Peace', 'Leo Tolstoy', '9780199232765', 3, 3);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('The Hobbit', 'J.R.R. Tolkien', '9780547928227', 10, 10);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('Fahrenheit 451', 'Ray Bradbury', '9781451673319', 6, 6);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('Brave New World', 'Aldous Huxley', '9780060850524', 5, 5);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('Crime and Punishment', 'Fyodor Dostoevsky', '9780143058144', 4, 4);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('The Lord of the Rings', 'J.R.R. Tolkien', '9780618640157', 9, 9);

INSERT INTO books (title, author, isbn, total_copies, available_copies)
VALUES ('Jane Eyre', 'Charlotte Brontë', '9780141441146', 5, 5);

