# 📚 Book Manager API

A production-ready RESTful API for managing books with Spring Boot, MySQL, Docker, and comprehensive testing; (Spring Boot, Java 21, 3 layers, exception handling, CRUD, Docker)

## ✨ Features

- **CRUD Operations**: Create, Read, Update, Delete books
- **Advanced Search**: Search by title, author, genre, and keywords
- **Validation**: Input validation with meaningful error messages
- **Exception Handling**: Global exception handling with consistent error responses
- **DTO Pattern**: Clean separation between API and persistence layers
- **Multi-Profile Support**: Local, Docker, and Test profiles
- **Container Ready**: Docker and Docker Compose support
- **Comprehensive Testing**: Unit tests for Repository, Service, and Controller layers

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.5.0 | Application framework |
| Spring Data JPA | - | Database access |
| MySQL | 8.0 | Production database |
| H2 | - | Testing database |
| Docker | - | Containerization |
| Maven | - | Build tool |
| Lombok | - | Boilerplate reduction |
| JUnit 5 | - | Testing framework |
| Mockito | - | Mocking framework |

## 📁 Project Structure

```
springboot-books-management-docker-mysql-app/
├── src/
│   ├── main/
│   │   ├── java/com/vbforge/booksapi/
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Database operations
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── request/     # Request DTOs
│   │   │   │   └── response/    # Response DTOs
│   │   │   ├── mapper/          # Entity-DTO mapping
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── helper/          # Docker docs, sql insert data, run endpoints example
│   │   └── resources/
│   │       ├── application.yml           # Main configuration
│   │       ├── application-local.yml     # Local profile
│   │       ├── application-docker.yml    # Docker profile
│   │       └── application-test.yml      # Test profile (in test package section)
│   └── test/                    # Unit and integration tests (repository, service, controller)
├── Dockerfile                   # Docker image configuration
├── docker-compose.yml          # Multi-container orchestration
├── pom.xml                     # Maven dependencies
└── README.md                   # Project documentation
```

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven** 3.6+
- **MySQL** 8.0 (for local development)
- **Docker** and **Docker Compose** (optional, for containerized deployment)

### Running Locally (Development)

1. **Clone the repository**
```bash
git clone <repository-url>
cd springboot-books-management-docker-mysql-app
```

2. **Configure MySQL**
```sql
CREATE DATABASE bookdb;
-- Credentials: username/password (configured in application-local.yml)
```

3. **Run the application**
```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or as a JAR
mvn clean package
java -jar target/*.jar --spring.profiles.active=local
```

### Running with Docker (Production-like)

1. **Start the application**
```bash
docker-compose up --build
```

2. **Run in detached mode**
```bash
docker-compose up --build -d
```

3. **Stop containers**
```bash
docker-compose down
```

4. **Stop and remove volumes (reset database)**
```bash
docker-compose down -v
```

### Running Tests

```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=BookControllerTest

# Run tests with coverage report
mvn clean test jacoco:report
```

## 📡 API Endpoints

### Base URL: `http://localhost:8080/api/book`

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| **GET** | `/` | Get all books | 200 OK |
| **GET** | `/{id}` | Get book by ID | 200 OK, 404 Not Found |
| **POST** | `/` | Create new book | 201 Created, 400 Bad Request, 409 Conflict |
| **PUT** | `/{id}` | Update book | 200 OK, 404 Not Found, 409 Conflict |
| **DELETE** | `/{id}` | Delete book | 204 No Content, 404 Not Found |
| **GET** | `/exists?title=X&author=Y` | Check if book exists | 200 OK |
| **GET** | `/genre/{genre}` | Get books by genre | 200 OK |
| **GET** | `/genre/{genre}/count` | Count books by genre | 200 OK |
| **GET** | `/search?keyword=X` | Search books | 200 OK |
| **GET** | `/advanced-search?title=X&author=Y` | Advanced search | 200 OK |

### Genre Values

```
Thriller, Romance, Fantasy, Fiction, Education
```

## 📝 API Examples

### Create a Book

**Request:**
```bash
POST /api/book
Content-Type: application/json

{
    "title": "The Pragmatic Programmer",
    "description": "A classic software development book",
    "author": "David Thomas",
    "genre": "Education"
}
```

**Response (201 Created):**
```json
{
    "id": 1,
    "title": "The Pragmatic Programmer",
    "description": "A classic software development book",
    "author": "David Thomas",
    "genre": "Education"
}
```

### Get All Books

**Request:**
```bash
GET /api/book
```

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "title": "The Pragmatic Programmer",
        "description": "A classic software development book",
        "author": "David Thomas",
        "genre": "Education"
    },
    {
        "id": 2,
        "title": "Harry Potter",
        "description": "A magical fantasy adventure",
        "author": "J.K. Rowling",
        "genre": "Fantasy"
    }
]
```

### Search Books

**Request:**
```bash
GET /api/book/search?keyword=Harry
```

**Response (200 OK):**
```json
[
    {
        "id": 2,
        "title": "Harry Potter",
        "description": "A magical fantasy adventure",
        "author": "J.K. Rowling",
        "genre": "Fantasy"
    }
]
```

### Error Response Example

**Request (Book not found):**
```bash
GET /api/book/999
```

**Response (404 Not Found):**
```json
{
    "errorCode": "BOOK_001",
    "message": "Book not found with id: 999",
    "statusCode": 404,
    "status": "Not Found",
    "path": "/api/book/999",
    "timestamp": "2026-04-06T15:30:00",
    "details": "The requested book resource could not be found"
}
```

## 🧪 Testing

### Test Coverage

| Layer | Test Class | Number of Tests |
|-------|-----------|-----------------|
| Repository | `BookRepositoryTest` | 19 |
| Service | `BooksServiceImplTest` | 22 |
| Controller | `BookControllerTest` | 27 |
| **Total** | | **68 tests** |

### Run Tests with Coverage

```bash
# Generate coverage report
mvn clean test jacoco:report

# Open coverage report
open target/site/jacoco/index.html
```

## 🐳 Docker Configuration

### Environment Profiles

| Profile | Database | Use Case |
|---------|----------|----------|
| `local` | MySQL (localhost:3306) | Local development |
| `docker` | MySQL (mysql:3306) | Docker deployment |
| `test` | H2 (in-memory) | Unit testing |

### Docker Commands

```bash
# Build and start containers
docker-compose up --build

# View logs
docker-compose logs -f app

# Stop containers
docker-compose down

# Reset everything (including database)
docker-compose down -v
```

## 🔐 Credentials Configuration

Database credentials are never committed to the repository. They are managed via a `.env` file which is gitignored.

**Setup:**

1. Copy the example file and fill in your values:
```bash
cp .env.example .env
```

2. Edit `.env` with your actual credentials:
```
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=bookdb
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_root_password
```

**How it works per profile:**

- **`local`** — credentials defined as fallback values inside `application-local.yml`, which is gitignored. Set `DB_USERNAME` / `DB_PASSWORD` as environment variables or IntelliJ run config to override the fallbacks.
- **`docker`** — credentials injected at runtime via `docker-compose.yml`, which reads them from `.env` automatically. No credentials exist in any committed file.
- **`test`** — uses H2 in-memory database, no credentials needed.

> `.env` and `application-local.yml` are both listed in `.gitignore` and must never be committed.

## 🔧 Configuration

### application.yml (Main)

```yaml
spring:
  profiles:
    active: local  # Change to 'docker' for containerized deployment
```

### Customizing Database Credentials

Edit the respective profile file:

- **Local**: `application-local.yml`
- **Docker**: `application-docker.yml`
- **Test**: `application-test.yml`

## 📊 Database Schema

```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    author VARCHAR(100) NOT NULL,
    genre ENUM('Thriller', 'Romance', 'Fantasy', 'Fiction', 'Education') NOT NULL
);
```

## 🎯 Key Features Implemented

- ✅ **Three-layer architecture** (Controller → Service → Repository)
- ✅ **DTO pattern** with MapStruct-like manual mapping
- ✅ **Global exception handling** with consistent error responses
- ✅ **Input validation** with Jakarta Validation
- ✅ **Comprehensive logging** at INFO, DEBUG, and ERROR levels
- ✅ **Transaction management** with `@Transactional`
- ✅ **Multi-profile configuration** (local, docker, test)
- ✅ **Docker support** with Dockerfile and docker-compose
- ✅ **Full test coverage** for all layers
- ✅ **Case-insensitive search** across title and author
- ✅ **Duplicate prevention** for books with same title and author

