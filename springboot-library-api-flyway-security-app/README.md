# **Library Management System API**

## **📋 Overview**
A **RESTful API** for a **Library Management System** built with **Spring Boot**, **JWT Authentication**, and **Role-Based Access Control**. The system allows:
- **Librarians** to manage books and users.
- **Users** to borrow/return books with **due dates**, **late fees**, and **borrowing limits**.
- [TASK REQUIREMENTS](documentation/TASK-REQUIREMENTS.md)

---

## **🛠 Tech Stack**
| Layer          | Technology               |
|----------------|--------------------------|
| **Backend**    | Spring Boot 3.x          |
| **Database**   | MySQL / H2 (for testing) |
| **ORM**        | Spring Data JPA          |
| **Migrations** | Flyway                   |
| **Security**   | Spring Security + JWT    |
| **Build Tool** | Maven                    |

---

## **📦 Dependencies**
```xml
<!-- Spring Boot Starter Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.owasp.encoder</groupId>
    <artifactId>encoder</artifactId>
    <version>1.2.3</version>
</dependency>
```

---

## **🚀 Features**
| Feature                     | Description                                                                 |
|-----------------------------|-----------------------------------------------------------------------------|
| **JWT Authentication**      | Secure signup/login with role-based access (`LIBRARIAN`/`USER`).          |
| **Book Management**         | CRUD operations for books (title, author, ISBN, copies).                   |
| **User Management**         | Librarians can update user roles.                                           |
| **Borrowing System**        | Users can borrow/return books with due dates and late fees ($1/day).      |
| **Validation**              | ISBN (13 digits), borrow limits (max 3 books), and input sanitization.      |
| **Pagination**              | Paginated endpoints for books/users (e.g., `/api/librarian/books?page=0`). |
| **Error Handling**          | Global exception handler with user-friendly messages.                     |

---

## **📂 Project Structure**
```
src/
├── main/
│   ├── java/com/vbforge/libraryapi/
│   │   ├── config/              # Security, CORS, and app config
│   │   ├── controller/          # REST controllers (Auth, Librarian, User)
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── entity/              # JPA entities (User, Book, BorrowRecord)
│   │   ├── exception/           # Custom exceptions and global handler
│   │   ├── repository/          # JPA repositories
│   │   ├── security/            # JWT utilities and filters
│   │   ├── service/             # Business logic (Auth, Book, User, Borrow)
│   │   └── LibraryApiApplication.java
│   ├── resources/
│   │   ├── db/migration/        # Flyway SQL migrations
│   │   └── application.yml      # App configuration
```

---

## **📋 API Endpoints**

### **Auth (Public)**
| Endpoint               | Method | Description                     | Request Body               | Response          |
|------------------------|--------|---------------------------------|----------------------------|--------------------|
| `/api/auth/signup`     | POST   | Register a new user            | `SignupRequest`             | `AuthResponse`     |
| `/api/auth/login`      | POST   | Login and get JWT token         | `LoginRequest`             | `AuthResponse`     |

### **Librarian (Requires `LIBRARIAN` Role)**
| Endpoint                     | Method | Description                     | Request Body               | Response          |
|------------------------------|--------|---------------------------------|----------------------------|--------------------|
| `/api/librarian/books`        | GET    | Get all books (paginated)       | -                          | `Page<BookResponse>` |
| `/api/librarian/books`        | POST   | Add a new book                  | `BookRequest`              | `BookResponse`     |
| `/api/librarian/books/{id}`   | PUT    | Update a book                   | `BookRequest`              | `BookResponse`     |
| `/api/librarian/books/{id}`   | DELETE | Delete a book                   | -                          | `204 No Content`   |
| `/api/librarian/books/search/title` | GET | Search books by title (paginated) | - | `Page<BookResponse>` |
| `/api/librarian/books/search/author` | GET | Search books by author (paginated) | - | `Page<BookResponse>` |
| `/api/librarian/users`        | GET    | Get all users                   | -                          | `List<UserResponse>` |
| `/api/librarian/users/{id}/role` | PUT | Update user role               | `UpdateRoleRequest`        | `UserResponse`     |

### **User (Requires `USER` Role)**
| Endpoint                     | Method | Description                     | Request Body               | Response          |
|------------------------------|--------|---------------------------------|----------------------------|--------------------|
| `/api/user/books/available`   | GET    | Get available books             | -                          | `List<BookResponse>` |
| `/api/user/books/borrow/{id}` | POST   | Borrow a book                   | -                          | `BorrowResponse`   |
| `/api/user/books/return/{id}` | POST   | Return a book                   | -                          | `BorrowResponse`   |
| `/api/user/records`           | GET    | Get user’s borrow records       | -                          | `List<BorrowResponse>` |

---

## **🔧 Setup Instructions**

### **1. Prerequisites**
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (or H2 for testing)
- Postman (for API testing)

### **2. Configure Database**
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate.ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### **3. Build and Run**
```bash
mvn clean install
mvn spring-boot:run
```

### **4. Test with Postman**
1. **Import the [Postman Collection](documentation/library-api-postman-collection.json)**.
2. **Signup/Login** to get a JWT token.
3. **Test endpoints** for librarians and users.
4. **Check [POSTMAN-INSTRUCTIONS](documentation/POSTMAN-INSTRUCTIONS.md) the instructions** 

---

## **🧪 Testing**
### **Unit Tests**
Run all tests:
```bash
mvn test
```
Key test classes:
- `AuthServiceTest`: Tests signup/login.
- `BookServiceTest`: Tests book CRUD and validation.
- `BorrowServiceTest`: Tests borrowing/returning logic.

### **Postman Collection**
[Download the Postman Collection](documentation/library-api-postman-collection.json)

---

## **📋 Business Logic**
### **Borrowing Rules**
- **Max 3 books** per user.
- **Due date**: 14 days from borrow date.
- **Late fee**: $1 per day after due date.
- **Book availability**: Decreases `availableCopies` when borrowed.

### **Librarian Actions**
- Only librarians can **add/update/delete books**.
- Only librarians can **update user roles**.

---

## **🔍 Error Handling**
| Error Scenario               | HTTP Status | Example Message                          |
|------------------------------|-------------|------------------------------------------|
| Invalid ISBN                 | 400         | "ISBN must be 13 digits"                  |
| Book Unavailable              | 400         | "Book 'Title' is not available"          |
| Borrow Limit Exceeded        | 400         | "Cannot borrow more than 3 books"        |
| Unauthorized Access          | 403         | "Access Denied"                          |
| User Not Found               | 404         | "User not found with ID: 1"              |

---

## **🎯 Evaluation Criteria**
| **Aspect**               | **What to Check**                                                   |
|--------------------------|---------------------------------------------------------------------|
| **Code Structure**       | Clean separation of concerns (controllers, services, repositories). |
| **Security**             | JWT authentication, role-based access, and input sanitization.    |
| **Business Logic**       | Borrowing/returning rules, late fees, and validation.               |
| **Validation**           | ISBN format, borrow limits, and input validation.                  |
| **Testing**              | Unit tests for services and controllers.                           |
| **Error Handling**       | User-friendly error messages for all edge cases.                    |

---

## **🚀 Bonus Challenges (Implemented)**
1. **Search & Filtering**:
    - `/api/librarian/books/search/title?title=Gatsby`
    - `/api/librarian/books/search/author?author=Fitzgerald`
2. **Pagination**:
    - `/api/librarian/books?page=0&size=10`
3. **Soft Delete**:
    - (Optional) Add `deleted` flag to entities for soft deletion.

---
