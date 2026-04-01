# **Task-Requirements: Library Management System API**

## **Objective**
Build a **RESTful API** for a **Library Management System** where:
- **Librarians** can manage books and users.
- **Users** can borrow and return books.
- The system enforces **due dates, late fees, and borrowing limits**.
- **challenging but achievable** backend task for a **strong Junior Java Developer**, focusing on **Spring Boot, security, and business logic**. 
- ability to design a **real-world API** with **authentication, authorization, and data validation**.

---

## Tech Stack

| Layer      | Technology      |
| ---------- | --------------- |
| Backend    | Spring Boot     |
| Database   | H2 / MySQL      |
| ORM        | Spring Data JPA |
| Migration  | Flyway          |
| Security   | Spring Security |
| Build Tool | Maven           |

---

## Dependencies:

* Spring Web
* Spring Data JPA
* Flyway
* Spring Security
* H2 / MySQL Driver
* Lombok

---

## **Requirements**

### **1. Entities & Database**
Design the following JPA entities with proper relationships:
- **`User`** (id, name, email, role: `USER`/`LIBRARIAN`, createdAt)
- **`Book`** (id, title, author, ISBN, totalCopies, availableCopies)
- **`BorrowRecord`** (id, user, book, borrowDate, dueDate, returnDate, lateFee)

Use **Flyway** for database migrations.

---

### **2. Authentication & Authorization**
- Use **JWT** for authentication.
- **Roles**:
  - `LIBRARIAN`: Can add/update/delete books and users.
  - `USER`: Can borrow/return books and view their records.
- Secure endpoints with **Spring Security**.

---

### **3. API Endpoints**

#### **Auth Controller** (Public)
| Endpoint               | Method | Description                     | Request Body               | Response          |
|------------------------|--------|---------------------------------|----------------------------|--------------------|
| `/api/auth/signup`     | POST   | Register a new user            | `SignupRequest`             | `AuthResponse`     |
| `/api/auth/login`      | POST   | Login and get JWT token         | `LoginRequest`             | `AuthResponse`     |

#### **Librarian Controller** (Requires `LIBRARIAN` role)
| Endpoint                     | Method | Description                     | Request Body               | Response          |
|------------------------------|--------|---------------------------------|----------------------------|--------------------|
| `/api/librarian/books`        | GET    | Get all books                   | -                          | `List<BookResponse>` |
| `/api/librarian/books`        | POST   | Add a new book                  | `BookRequest`              | `BookResponse`     |
| `/api/librarian/books/{id}`   | PUT    | Update a book                  | `BookRequest`              | `BookResponse`     |
| `/api/librarian/books/{id}`   | DELETE | Delete a book                  | -                          | `204 No Content`   |
| `/api/librarian/users`        | GET    | Get all users                   | -                          | `List<UserResponse>` |
| `/api/librarian/users/{id}`   | PUT    | Update a user’s role           | `UpdateRoleRequest`        | `UserResponse`     |

#### **User Controller** (Requires `USER` role)
| Endpoint                     | Method | Description                     | Request Body               | Response          |
|------------------------------|--------|---------------------------------|----------------------------|--------------------|
| `/api/user/books/available`   | GET    | Get available books             | -                          | `List<BookResponse>` |
| `/api/user/books/borrow/{id}` | POST   | Borrow a book                   | -                          | `BorrowResponse`   |
| `/api/user/books/return/{id}` | POST   | Return a book                   | -                          | `BorrowResponse`   |
| `/api/user/records`          | GET    | Get user’s borrow records       | -                          | `List<BorrowResponse>` |

---

### **4. Business Logic & Validation**
Implement the following rules:
1. **Borrowing Books**:
   - A user can borrow **max 3 books at a time**.
   - If a book is **not available**, return `400 Bad Request`.
   - Set **due date = borrow date + 14 days**.
   - Decrease `availableCopies` when borrowed.

2. **Returning Books**:
   - If returned **after due date**, calculate **late fee: $1 per day**.
   - Increase `availableCopies` when returned.

3. **Librarian Actions**:
   - Only librarians can **add/update/delete books**.
   - Only librarians can **update user roles**.

---

### **5. DTOs (Request/Response)**
Define the following DTOs:
- `SignupRequest` (name, email, password)
- `LoginRequest` (email, password)
- `AuthResponse` (token, user details)
- `BookRequest` (title, author, ISBN, totalCopies)
- `BookResponse` (id, title, author, availableCopies)
- `BorrowResponse` (book, borrowDate, dueDate, returnDate, lateFee)
- `UpdateRoleRequest` (role: `USER`/`LIBRARIAN`)

---

### **6. Error Handling**
Return **user-friendly error messages** for:
- Invalid inputs (e.g., `ISBN must be 13 digits`).
- Business rule violations (e.g., `Cannot borrow: max limit reached`).
- Unauthorized access (e.g., `Only librarians can add books`).

Use a **global exception handler**.

---

### **7. Testing**
Write **unit tests** for:
- **Service layer**: Test borrowing/returning books, role updates.
- **Controller layer**: Test endpoint security and responses.
- **Edge cases**: Max borrow limit, late fees, invalid ISBN.

---

## **Summary of key Components**
| Feature         | Repository             | Service       | DTOs                                 | Exceptions                                                 |
|-----------------|------------------------|---------------|--------------------------------------|------------------------------------------------------------|
| Book Management | BookRepository         | BookService   | BookRequest, BookResponse            | BookNotFoundException, IsbnValidationException             |
| User Management | UserRepository         | UserService   | UpdateUserRequest, UpdateRoleRequest | UserNotFoundException, RoleUpdateException                 |
| Borrowing       | BorrowRecordRepository | BorrowService | BorrowResponse                       | BorrowLimitExceededException, BookAlreadyBorrowedException |

---
