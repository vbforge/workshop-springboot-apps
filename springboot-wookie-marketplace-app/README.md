# Wookie Books Marketplace API

A RESTful bookstore API for the Wookie community to self-publish and sell adventure stories. Built with Spring Boot 3.5.0 and Java 21.

---

## Table of Contents

  * [Overview](#overview)
  * [REQUIREMENTS](REQUIREMENTS.md)
  * [Technology Stack](#technology-stack)
  * [Getting Started](#getting-started)
  * [Authentication](#authentication)
  * [API Endpoints](#api-endpoints)
  * [Database Schema](#database-schema)
  * [Role-Based Access Control](#role-based-access-control)
  * [Error Handling](#error-handling)
  * [Testing](#testing)
  * [Docker Deployment](#docker-deployment)
  * [Example Workflows](#example-workflows)
  * [Complete Endpoint Summary](#complete-endpoint-summary)

---

## Overview

Wookie Books Marketplace allows authors to publish their adventures, manage their books, and sell them to other Wookies. The platform features JWT-based authentication, role-based access control, and comprehensive book management capabilities.

### Key Features

- **User Management**: Registration, authentication, and profile management
- **Book Management**: Create, read, update, delete (soft/hard) operations
- **Search & Filter**: Find books by title, author, or price range
- **Role-Based Access**: USER, RESTRICTED_USER (Darth Vader), and SUPER_ADMIN roles
- **JWT Authentication**: Secure stateless authentication
- **Database Migrations**: Flyway for version-controlled schema management

---

## Technology Stack

- **Java 21** - Core language
- **Spring Boot 3.5.0** - Application framework
- **Spring Security 6.5.0** - Authentication & authorization
- **Spring Data JPA** - Database persistence
- **MySQL 8.0** - Production database
- **H2 Database** - Testing database
- **Flyway** - Database migrations
- **JWT (JJWT 0.12.5)** - Token-based authentication
- **Maven** - Build automation
- **Testcontainers** - Integration testing
- **Lombok** - Boilerplate reduction

---

## Getting Started

### Prerequisites

- JDK 21 or later
- MySQL 8.0 or later
- Maven 3.8+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/wookie-books-marketplace.git
cd wookie-books-marketplace
```

2. **Configure environment variables**
   Create a `.env` file in the project root:
```properties
SPRING_PROFILES_ACTIVE=dev
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET_KEY=your-256-bit-secret-key-min-32-chars
JWT_EXPIRATION=86400000
RESTRICTED_USERNAME=DarthVader
```

3. **Create the database**
```sql
CREATE DATABASE wookie_books CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

4. **Build and run**
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8083`

### Running with Different Profiles

```bash
# Development profile (default)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Docker profile
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Test profile
mvn test -Dspring.profiles.active=test
```

---

## Authentication

The API uses JWT (JSON Web Token) for authentication. To access protected endpoints, include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Get a Token

```bash
POST /api/token
Content-Type: application/json

{
    "username": "your_username",
    "password": "your_password"
}
```

### Default Users

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| Lohgarra | admin123 | SUPER_ADMIN | Platform administrator |
| TestAuthor | test123  | USER | Regular author |
| DarthVader | darth123 | RESTRICTED_USER | Cannot publish books |

---

## API Endpoints

### User Management (`/api/user`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/register` | Register new user | Public |
| GET | `/{userId}` | Get user by ID | Owner or Admin |
| GET | `/pseudonym/{authorPseudonym}` | Get user by pseudonym | Owner or Admin |
| PUT | `/pseudonym/{currentPseudonym}` | Update pseudonym | Owner or Admin |
| PUT | `/{userId}` | Update user details | Owner or Admin |
| DELETE | `/{userId}` | Soft delete user | Owner or Admin |
| GET | `/` | Get all users | SUPER_ADMIN only |
| GET | `/book/{title}` | Find user by book title | Public |
| GET | `/me/books` | Get current user's books | Authenticated |

### Book Management (`/api/wookie_books`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/` | Search books with filters | Public |
| GET | `/{bookId}` | Get book by ID | Public |
| GET | `/title/{title}` | Get book by title | Public |
| POST | `/` | Create new book | Authenticated (except RESTRICTED_USER) |
| PUT | `/{bookId}` | Update book | Owner or Admin |
| DELETE | `/` | Soft delete (unpublish) by title | Owner or Admin |
| PATCH | `/{bookId}/publish` | Toggle publish status | Owner or Admin |
| GET | `/me/books` | Get my books | Authenticated |
| GET | `/admin/all` | Get all books | SUPER_ADMIN only |
| DELETE | `/admin/{bookId}` | Hard delete book | SUPER_ADMIN only |
| GET | `/admin/unpublished` | Get all unpublished books | SUPER_ADMIN only |
| GET | `/admin/cleanup-candidates` | Get old unpublished books | SUPER_ADMIN only |

### Authentication (`/api/token`)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/` | Generate JWT token | Public |

### Search Parameters for Books

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| authorPseudonym | String | Filter by author | `?authorPseudonym=Lohgarra` |
| title | String | Partial title match | `?title=Adventure` |
| minPrice | BigDecimal | Minimum price | `?minPrice=10.00` |
| maxPrice | BigDecimal | Maximum price | `?maxPrice=50.00` |
| page | Integer | Page number (0-indexed) | `?page=0` |
| size | Integer | Page size | `?size=20` |
| sort | String | Sort by field | `?sort=price,desc` |

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_pseudonym VARCHAR(100) NOT NULL UNIQUE,
    author_password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Books Table
```sql
CREATE TABLE books (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    cover_image TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
);
```

---

## Role-Based Access Control

| Role | Permissions |
|------|-------------|
| **USER** | Register, authenticate, create/update/delete own books, view public books |
| **RESTRICTED_USER** | Register, authenticate, view public books, CANNOT publish books |
| **SUPER_ADMIN** | All USER permissions + manage all books, hard delete, view all users |

### Special Rules

- **Darth Vader** (RESTRICTED_USER) cannot publish any books
- Users can only modify their own books
- Books are soft-deleted by default (unpublished)
- Only SUPER_ADMIN can permanently delete books

---

## Error Handling

The API returns standard HTTP status codes and detailed error responses:

```json
{
    "timestamp": "2024-01-01T12:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "details": {
        "title": "Title is required"
    }
}
```

### Common Status Codes

| Status | Description |
|--------|-------------|
| 200 OK | Request successful |
| 201 Created | Resource created |
| 204 No Content | Resource deleted |
| 400 Bad Request | Invalid input |
| 401 Unauthorized | Authentication required |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Resource not found |
| 409 Conflict | Duplicate resource |

---

## Testing

### Run Tests
```bash
# Unit and integration tests
mvn test

# With coverage report
mvn test jacoco:report
```

### Test Coverage
- **Service Layer**: Unit tests with Mockito
- **Controller Layer**: Integration tests with MockMvc
- **Repository Layer**: Testcontainers with MySQL
- **Security Layer**: Authentication and authorization tests

#### 1. **Service Layer Tests** (Unit Tests with Mockito)
- UserService tests (CRUD, validation, edge cases)
- BookService tests (business logic, permissions, Darth Vader restriction)
- JwtService tests (token generation, validation)

#### 2. **Controller Layer Tests** (Integration Tests with MockMvc)
- UserController tests (endpoint responses, validation)
- BookController tests (CRUD operations, authorization)
- TokenController tests (authentication flow)
- Global exception handling tests

#### 3. **Repository Layer Tests** (Testcontainers)
- Custom query tests
- Pagination tests
- Search functionality tests

#### 4. **Security Tests**
- Unauthorized access attempts
- Role-based permission tests
- JWT filter tests

### Test Matrix:

| Component | Unit Tests | Integration Tests | Security Tests |
|-----------|------------|-------------------|----------------|
| UserService | ✅ | - | - |
| BookService | ✅ | - | - |
| UserController | - | ✅ | ✅ |
| BookController | - | ✅ | ✅ |
| JwtService | ✅ | - | - |
| Repositories | - | ✅ (Testcontainers) | - |


---

## Docker Deployment

### Build Docker Image
```bash
docker build -t wookie-books-api .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Environment Variables for Docker
```properties
SPRING_PROFILES_ACTIVE=docker
DB_HOST=mysql
DB_PORT=3306
DB_NAME=wookie_books
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET_KEY=your-secret-key
```

---

## Example Workflows

### 1. Author Publishes a Book

```bash
# Register
POST /api/user/register
{"authorPseudonym": "new_author", "authorPassword": "password123"}

# Authenticate
POST /api/token
{"username": "new_author", "password": "password123"}

# Create book
POST /api/wookie_books
Authorization: Bearer <token>
{
    "title": "My Adventure",
    "description": "An epic journey...",
    "coverImage": "https://example.com/cover.jpg",
    "price": 19.99
}
```

### 2. Admin Hard Deletes Old Books

```bash
# Authenticate as admin
POST /api/token
{"username": "Lohgarra", "password": "admin123"}

# Get cleanup candidates
GET /api/wookie_books/admin/cleanup-candidates?daysOld=90
Authorization: Bearer <admin-token>

# Hard delete specific book
DELETE /api/wookie_books/admin/1
Authorization: Bearer <admin-token>
```

---

## Complete Endpoint Summary

### 🟢 Public Endpoints (No Authentication)

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1 | POST | `/api/user/register` | Register new user |
| 2 | POST | `/api/token` | Authenticate & get JWT token |
| 3 | GET | `/api/wookie_books` | Search books (paginated) |
| 4 | GET | `/api/wookie_books/{bookId}` | Get book by ID |
| 5 | GET | `/api/wookie_books/title/{title}` | Get book by title |
| 6 | GET | `/api/user/book/{title}` | Find author by book title |

### 🔵 Authenticated Endpoints (USER, RESTRICTED_USER, SUPER_ADMIN)

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 7 | GET | `/api/user/{userId}` | Get user profile (own or admin) |
| 8 | GET | `/api/user/pseudonym/{authorPseudonym}` | Get user by pseudonym (own or admin) |
| 9 | PUT | `/api/user/pseudonym/{currentPseudonym}` | Update pseudonym (own or admin) |
| 10 | PUT | `/api/user/{userId}` | Update user (own or admin) |
| 11 | DELETE | `/api/user/{userId}` | Soft delete user (own or admin) |
| 12 | GET | `/api/user/me/books` | Get current user's books |
| 13 | POST | `/api/wookie_books` | Create new book (RESTRICTED_USER excluded) |
| 14 | PUT | `/api/wookie_books/{bookId}` | Update book (owner or admin) |
| 15 | DELETE | `/api/wookie_books` | Soft delete book by title (owner or admin) |
| 16 | PATCH | `/api/wookie_books/{bookId}/publish` | Toggle publish status (owner or admin) |

### 🔴 Admin-Only Endpoints (SUPER_ADMIN only)

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 17 | GET | `/api/user` | Get all users |
| 18 | GET | `/api/wookie_books/admin/all` | Get all books (including unpublished) |
| 19 | DELETE | `/api/wookie_books/admin/{bookId}` | Hard delete book permanently |
| 20 | GET | `/api/wookie_books/admin/unpublished` | Get all unpublished books |
| 21 | GET | `/api/wookie_books/admin/cleanup-candidates` | Get old unpublished books (cleanup candidates) |

### 🟡 Utility Endpoints

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 22 | GET | `/api/test/auth-info` | Debug authentication info (dev only) |
| 23 | POST | `/api/helper/encode` | Encode password helper (dev only) |

---
