# 🧪 Testing Guide - Book Management REST API

Complete guide for testing the Book Management API using various tools.

---

## 📋 Table of Contents

  * [🎨 Testing with Swagger UI](#-testing-with-swagger-ui)
  * [📮 Testing with Postman](#-testing-with-postman)
  * [💻 Testing with cURL](#-testing-with-curl)
  * [🤖 Automated Tests](#-automated-tests)
  * [✅ Test Checklist](#-test-checklist)
  * [📊 Expected Response Times](#-expected-response-times)
  * [🐛 Common Issues & Solutions](#-common-issues--solutions)
  * [📝 Tips for Effective Testing](#-tips-for-effective-testing)

---

## 🎨 Testing with Swagger UI

### Getting Started

1. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

2. **Open Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **Navigate through endpoints** organized by tags:
   - API Info
   - Books
   - Authors
   - Categories
   - Publishers

### How to Test an Endpoint

1. **Click on an endpoint** to expand it
2. **Click "Try it out"** button
3. **Fill in required parameters**
4. **Click "Execute"**
5. **View the response** below

### Example: Create a Book

1. Go to **Books** section
2. Find **POST /api/books**
3. Click **Try it out**
4. Modify the request body:
```json
{
  "isbn": "978-0-987654-32-1",
  "title": "My Test Book",
  "description": "A great book for testing",
  "publicationDate": "2024-01-15",
  "price": 24.99,
  "stockQuantity": 50,
  "language": "English",
  "pageCount": 320,
  "categoryId": 1,
  "publisherId": 1,
  "authorIds": [1]
}
```
5. Click **Execute**
6. Check the response (should be 201 Created)

---

## 📮 Testing with Postman

### Setup

1. **Import Collection**
   - Open Postman
   - Click **Import**
   - Select `Book-Management-API.postman_collection.json`

2. **Import Environment**
   - Click on **Environments** (left sidebar)
   - Click **Import**
   - Select `Book-Management-Local.postman_environment.json`

3. **Select Environment**
   - Top right corner dropdown
   - Select "Book Management - Local"

### Running Requests

#### 1. Health Check (First Test)

```
GET {{baseUrl}}/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "application": "Book Management API",
  "timestamp": "2024-11-04T15:30:45"
}
```

#### 2. Get All Books

```
GET {{baseUrl}}/books?page=0&size=10&sortBy=title&direction=ASC
```

**Parameters:**
- `page`: 0 (first page)
- `size`: 10 (items per page)
- `sortBy`: title
- `direction`: ASC

#### 3. Create a Book

```
POST {{baseUrl}}/books
Content-Type: application/json

{
  "isbn": "978-0-111111-11-1",
  "title": "Postman Test Book",
  "price": 19.99,
  "stockQuantity": 100,
  "categoryId": 1,
  "publisherId": 1,
  "authorIds": [1]
}
```

**Save the ID from response** for subsequent tests.

#### 4. Get Book by ID

```
GET {{baseUrl}}/books/{{bookId}}
```

**Note:** Replace `{{bookId}}` with actual ID or set it in environment variables.

#### 5. Update Book

```
PUT {{baseUrl}}/books/{{bookId}}
Content-Type: application/json

{
  "title": "Updated Title",
  "price": 29.99
}
```

#### 6. Delete Book

```
DELETE {{baseUrl}}/books/{{bookId}}
```

### Using Collection Runner

1. Click **Collections** → **Book Management REST API**
2. Click **Run** (▶️ button)
3. Select requests to run
4. Click **Run Book Management REST API**
5. View test results

---

## 💻 Testing with cURL

### Basic Commands

#### Get All Books
```bash
curl -X GET "http://localhost:8080/api/books?page=0&size=10" \
  -H "accept: application/json"
```

#### Get Book by ID
```bash
curl -X GET "http://localhost:8080/api/books/1" \
  -H "accept: application/json"
```

#### Create Book
```bash
curl -X POST "http://localhost:8080/api/books" \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "978-0-123456-78-9",
    "title": "cURL Test Book",
    "price": 19.99,
    "stockQuantity": 50,
    "categoryId": 1,
    "publisherId": 1,
    "authorIds": [1]
  }'
```

#### Update Book
```bash
curl -X PUT "http://localhost:8080/api/books/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated via cURL",
    "price": 24.99
  }'
```

#### Delete Book
```bash
curl -X DELETE "http://localhost:8080/api/books/1"
```

#### Search Books
```bash
curl -X GET "http://localhost:8080/api/books/search?keyword=harry&page=0&size=10"
```

---

## 🤖 Automated Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
# Service tests
mvn test -Dtest=BookServiceImplTest

# Controller tests
mvn test -Dtest=BookControllerTest

# Repository tests
mvn test -Dtest=BookRepositoryTest
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

View report: `target/site/jacoco/index.html`

### Test Categories

#### Unit Tests (Service Layer)
```bash
mvn test -Dtest=*ServiceImplTest
```

Tests business logic with mocked dependencies.

#### Integration Tests (Controllers)
```bash
mvn test -Dtest=*ControllerTest
```

Tests REST endpoints with MockMvc.

#### Repository Tests
```bash
mvn test -Dtest=*RepositoryTest
```

Tests database operations with H2.

---

## 🎯 Common Test Scenarios

### Scenario 1: Complete CRUD Flow

1. **Create** a new book
   ```
   POST /api/books
   ```

2. **Read** the created book
   ```
   GET /api/books/{id}
   ```

3. **Update** the book
   ```
   PUT /api/books/{id}
   ```

4. **Delete** the book
   ```
   DELETE /api/books/{id}
   ```

5. **Verify deletion**
   ```
   GET /api/books/{id}
   → Should return 404
   ```

### Scenario 2: Search and Filter

1. **Search by keyword**
   ```
   GET /api/books/search?keyword=harry
   ```

2. **Filter by category**
   ```
   GET /api/books/category/1
   ```

3. **Filter by price range**
   ```
   GET /api/books/price-range?minPrice=10&maxPrice=30
   ```

4. **Get books in stock**
   ```
   GET /api/books/in-stock
   ```

### Scenario 3: Pagination Testing

1. **Get first page**
   ```
   GET /api/books?page=0&size=5
   ```

2. **Get second page**
   ```
   GET /api/books?page=1&size=5
   ```

3. **Sort by price ascending**
   ```
   GET /api/books?sortBy=price&direction=ASC
   ```

4. **Sort by title descending**
   ```
   GET /api/books?sortBy=title&direction=DESC
   ```

### Scenario 4: Validation Testing

1. **Create book with missing required fields**
   ```json
   POST /api/books
   {
     "isbn": "",
     "title": ""
   }
   → Should return 400 with validation errors
   ```

2. **Create book with invalid price**
   ```json
   {
     "price": -10.00
   }
   → Should return 400
   ```

3. **Create book with duplicate ISBN**
   ```json
   {
     "isbn": "978-0-7475-3269-9"
   }
   → Should return 409 Conflict
   ```

### Scenario 5: Stock Management

1. **Check current stock**
   ```
   GET /api/books/1
   ```

2. **Update stock quantity**
   ```
   PATCH /api/books/1/stock?quantity=150
   ```

3. **Verify update**
   ```
   GET /api/books/1
   → stockQuantity should be 150
   ```

4. **Get low stock books**
   ```
   GET /api/books/low-stock?threshold=10
   ```

---

## ✅ Test Checklist

### Books API
- [ ] Create book successfully
- [ ] Get book by ID
- [ ] Get book by ISBN
- [ ] Get all books with pagination
- [ ] Update book
- [ ] Update stock quantity
- [ ] Delete book
- [ ] Search books by keyword
- [ ] Filter by category
- [ ] Filter by author
- [ ] Filter by publisher
- [ ] Filter by price range
- [ ] Get books in stock
- [ ] Get books with low stock

### Authors API
- [ ] Create author
- [ ] Get all authors
- [ ] Get author by ID
- [ ] Update author
- [ ] Delete author
- [ ] Search authors by name
- [ ] Get authors by nationality
- [ ] Get books by author

### Categories API
- [ ] Create category
- [ ] Get all categories
- [ ] Get category by ID
- [ ] Update category
- [ ] Delete category
- [ ] Search categories
- [ ] Get categories with books

### Publishers API
- [ ] Create publisher
- [ ] Get all publishers
- [ ] Get publisher by ID
- [ ] Update publisher
- [ ] Delete publisher
- [ ] Search publishers

### Error Handling
- [ ] 404 Not Found for non-existent resources
- [ ] 400 Bad Request for validation errors
- [ ] 409 Conflict for duplicates
- [ ] Proper error messages

---

## 📊 Expected Response Times

| Operation | Expected Time |
|-----------|---------------|
| GET (single) | < 100ms |
| GET (list) | < 200ms |
| POST | < 300ms |
| PUT | < 300ms |
| DELETE | < 200ms |
| Search | < 500ms |

---

## 🐛 Common Issues & Solutions

### Issue 1: Connection Refused
**Solution:** Ensure the application is running on port 8080

### Issue 2: 404 Not Found
**Solution:** Check the endpoint URL and HTTP method

### Issue 3: 400 Validation Error
**Solution:** Review request body and ensure all required fields are present

### Issue 4: 409 Conflict
**Solution:** ISBN or name already exists, use unique values

### Issue 5: 500 Internal Server Error
**Solution:** Check application logs for stack trace

---

## 📝 Tips for Effective Testing

1. **Start Simple** - Test health check first
2. **Use Variables** - Store IDs in Postman environment
3. **Test Sequentially** - Follow CRUD flow
4. **Verify Responses** - Check status codes and response bodies
5. **Test Edge Cases** - Empty strings, negative numbers, duplicates
6. **Clean Up** - Delete test data after testing
7. **Use Swagger** - For quick exploratory testing
8. **Use Postman** - For comprehensive test suites
9. **Use cURL** - For scripting and automation
10. **Run Automated Tests** - Before committing code

---

**Happy Testing! 🧪✨**