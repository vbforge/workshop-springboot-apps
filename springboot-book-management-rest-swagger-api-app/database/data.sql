-- ====================================
-- Book Management API - Sample Data
-- ====================================

USE book_management_db;

-- ====================================
-- Insert Authors
-- ====================================
INSERT INTO authors (name, biography, date_of_birth, nationality)
VALUES ('J.K. Rowling', 'British author best known for the Harry Potter series', '1965-07-31', 'British'),
       ('George Orwell', 'English novelist and essayist, journalist and critic', '1903-06-25', 'British'),
       ('Jane Austen', 'English novelist known for her six major novels', '1775-12-16', 'British'),
       ('Stephen King',
        'American author of horror, supernatural fiction, suspense, crime, science-fiction, and fantasy novels',
        '1947-09-21', 'American'),
       ('Agatha Christie', 'English writer known for her 66 detective novels', '1890-09-15', 'British'),
       ('Ernest Hemingway', 'American novelist, short-story writer, and journalist', '1899-07-21', 'American'),
       ('F. Scott Fitzgerald', 'American novelist and short story writer', '1896-09-24', 'American'),
       ('Harper Lee', 'American novelist best known for To Kill a Mockingbird', '1926-04-28', 'American');

-- ====================================
-- Insert Categories
-- ====================================
INSERT INTO categories (name, description)
VALUES ('Fiction', 'Literary works based on imagination'),
       ('Non-Fiction', 'Factual and informative works'),
       ('Science Fiction', 'Fiction based on imagined future scientific advances'),
       ('Fantasy', 'Fiction involving magic and adventure'),
       ('Mystery', 'Fiction dealing with the solution of a crime'),
       ('Thriller', 'Fiction characterized by fast pacing and tension'),
       ('Romance', 'Fiction focused on romantic relationships'),
       ('Horror', 'Fiction intended to frighten or scare'),
       ('Biography', 'Non-fiction account of someone\'s life'),
       ('History', 'Non-fiction account of past events'),
       ('Self-Help', 'Books offering advice and guidance'),
       ('Classic', 'Literature of recognized high quality');

-- ====================================
-- Insert Publishers
-- ====================================
INSERT INTO publishers (name, address, website, contact_email)
VALUES ('Penguin Random House', 'New York, USA', 'https://www.penguinrandomhouse.com', 'info@penguinrandomhouse.com'),
       ('HarperCollins', 'New York, USA', 'https://www.harpercollins.com', 'contact@harpercollins.com'),
       ('Simon & Schuster', 'New York, USA', 'https://www.simonandschuster.com', 'info@simonandschuster.com'),
       ('Macmillan Publishers', 'London, UK', 'https://www.macmillan.com', 'info@macmillan.com'),
       ('Hachette Book Group', 'New York, USA', 'https://www.hachettebookgroup.com', 'contact@hachette.com'),
       ('Bloomsbury Publishing', 'London, UK', 'https://www.bloomsbury.com', 'info@bloomsbury.com');

-- ====================================
-- Insert Books
-- ====================================
INSERT INTO books (isbn, title, description, publication_date, price, stock_quantity, language, page_count, category_id,
                   publisher_id)
VALUES ('978-0-7475-3269-9', 'Harry Potter and the Philosopher''s Stone', 'The first novel in the Harry Potter series',
        '1997-06-26', 19.99, 150, 'English', 223, 4, 6),
       ('978-0-452-28423-4', '1984', 'Dystopian social science fiction novel', '1949-06-08', 14.99, 200, 'English', 328,
        3, 1),
       ('978-0-14-143951-8', 'Pride and Prejudice', 'Romantic novel of manners', '1813-01-28', 12.99, 180, 'English',
        432, 12, 1),
       ('978-0-385-12167-5', 'The Shining', 'Horror novel about a family isolated in a haunted hotel', '1977-01-28',
        16.99, 120, 'English', 447, 8, 3),
       ('978-0-06-112008-4', 'To Kill a Mockingbird', 'Novel about racial injustice in the American South',
        '1960-07-11', 15.99, 220, 'English', 324, 12, 2),
       ('978-0-00-752450-1', 'Murder on the Orient Express', 'Detective novel featuring Hercule Poirot', '1934-01-01',
        13.99, 175, 'English', 256, 5, 2),
       ('978-0-684-80122-3', 'The Old Man and the Sea', 'Short novel about an aging fisherman', '1952-09-01', 11.99,
        190, 'English', 127, 12, 3),
       ('978-0-7432-7356-5', 'The Great Gatsby', 'Novel about the American Dream', '1925-04-10', 13.99, 210, 'English',
        180, 12, 4),
       ('978-0-7475-4215-5', 'Harry Potter and the Chamber of Secrets', 'The second novel in the Harry Potter series',
        '1998-07-02', 19.99, 145, 'English', 251, 4, 6),
       ('978-0-7475-4629-0', 'Harry Potter and the Prisoner of Azkaban', 'The third novel in the Harry Potter series',
        '1999-07-08', 20.99, 140, 'English', 317, 4, 6);

-- ====================================
-- Link Books to Authors (Many-to-Many)
-- ====================================
INSERT INTO book_authors (book_id, author_id)
VALUES (1, 1), -- Harry Potter 1 - J.K. Rowling
       (2, 2), -- 1984 - George Orwell
       (3, 3), -- Pride and Prejudice - Jane Austen
       (4, 4), -- The Shining - Stephen King
       (5, 8), -- To Kill a Mockingbird - Harper Lee
       (6, 5), -- Murder on the Orient Express - Agatha Christie
       (7, 6), -- The Old Man and the Sea - Hemingway
       (8, 7), -- The Great Gatsby - F. Scott Fitzgerald
       (9, 1), -- Harry Potter 2 - J.K. Rowling
       (10, 1); -- Harry Potter 3 - J.K. Rowling