# 📚 Book Management REST API - Documentation

## 🚀 Quick Start

### Access Swagger UI
Once the application is running, access the interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

### Access OpenAPI JSON
The OpenAPI specification is available at:

```
http://localhost:8080/api-docs
```

---

## 📋 API Endpoints Overview

### 🏠 API Info
- `GET /api` - API welcome message
- `GET /api/health` - Health check
- `GET /api/version` - Version information

### 📚 Books (`/api/books`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/books` | Create a new book |
| GET | `/api/books` | Get all books (paginated) |
| GET | `/api/books/{id}` | Get book by ID |
| GET | `/api/books/isbn/{isbn}` | Get book by ISBN |
| PUT | `/api/books/{id}` | Update book |
| PATCH | `/api/books/{id}/stock` | Update stock quantity |
| DELETE | `/api/books/{id}` | Delete book |
| GET | `/api/books/search` | Search books |
| GET | `/api/books/category/{categoryId}` | Get books by category |
| GET | `/api/books/author/{authorId}` | Get books by author |
| GET | `/api/books/publisher/{publisherId}` | Get books by publisher |
| GET | `/api/books/price-range` | Get books by price range |
| GET | `/api/books/in-stock` | Get books in stock |
| GET | `/api/books/low-stock` | Get books with low stock |

### 👤 Authors (`/api/authors`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/authors` | Create a new author |
| GET | `/api/authors` | Get all authors |
| GET | `/api/authors/{id}` | Get author by ID |
| PUT | `/api/authors/{id}` | Update author |
| DELETE | `/api/authors/{id}` | Delete author |
| GET | `/api/authors/search` | Search authors |
| GET | `/api/authors/nationality/{nationality}` | Get authors by nationality |
| GET | `/api/authors/{id}/books` | Get books by author |

### 📂 Categories (`/api/categories`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create a new category |
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/{id}` | Get category by ID |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |
| GET | `/api/categories/search` | Search categories |
| GET | `/api/categories/with-books` | Get categories with books |

### 🏢 Publishers (`/api/publishers`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/publishers` | Create a new publisher |
| GET | `/api/publishers` | Get all publishers |
| GET | `/api/publishers/{id}` | Get publisher by ID |
| PUT | `/api/publishers/{id}` | Update publisher |
| DELETE | `/api/publishers/{id}` | Delete publisher |
| GET | `/api/publishers/search` | Search publishers |

---

## 📝 Request/Response Examples

### Create a Book
**POST** `/api/books`

**Request Body:**
```json
{
  "isbn": "978-0-123456-78-9",
  "title": "Sample Book Title",
  "description": "A fascinating book about...",
  "publicationDate": "2024-01-15",
  "price": 29.99,
  "stockQuantity": 100,
  "language": "English",
  "pageCount": 350,
  "categoryId": 1,
  "publisherId": 2,
  "authorIds": [1, 2]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Book created successfully",
  "data": {
    "id": 1,
    "isbn": "978-0-123456-78-9",
    "title": "Sample Book Title",
    "description": "A fascinating book about...",
    "publicationDate": "2024-01-15",
    "price": 29.99,
    "stockQuantity": 100,
    "language": "English",
    "pageCount": 350,
    "category": {
      "id": 1,
      "name": "Fiction"
    },
    "publisher": {
      "id": 2,
      "name": "Sample Publisher"
    },
    "authors": [
      {
        "id": 1,
        "name": "John Doe"
      }
    ],
    "createdAt": "2024-11-04T15:30:45",
    "updatedAt": "2024-11-04T15:30:45"
  },
  "timestamp": "2024-11-04T15:30:45"
}
```

### Get Books with Pagination
**GET** `/api/books?page=0&size=10&sortBy=title&direction=ASC`

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "isbn": "978-0-123456-78-9",
        "title": "Book Title 1",
        ...
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "first": true,
    "last": false,
    "empty": false
  },
  "timestamp": "2024-11-04T15:30:45"
}
```

### Search Books
**GET** `/api/books/search?keyword=harry&page=0&size=10`

### Error Response Example
**Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Book not found with id: '999'",
  "error": "Resource Not Found",
  "status": 404,
  "path": "/api/books/999",
  "timestamp": "2024-11-04T15:30:45"
}
```

**Validation Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validation failed",
  "error": "Validation Error",
  "status": 400,
  "path": "/api/books",
  "timestamp": "2024-11-04T15:30:45",
  "validationErrors": [
    {
      "field": "title",
      "message": "Title is required",
      "rejectedValue": null
    }
  ]
}
```

---

## 🔍 Query Parameters

### Pagination
- `page` - Page number (0-indexed, default: 0)
- `size` - Page size (default: 10)
- `sortBy` - Field to sort by (default: id)
- `direction` - Sort direction: ASC or DESC (default: ASC)

**Example:**
```
GET /api/books?page=1&size=20&sortBy=price&direction=DESC
```

### Search
- `keyword` - Search term for books (searches title, description, author name)
- `name` - Search term for authors/categories/publishers

---

## 🎯 HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Validation errors, invalid input |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (e.g., ISBN already exists) |
| 500 | Internal Server Error | Unexpected server error |

---

## 🛠️ Testing with Swagger UI

1. **Start the application**
2. **Open browser** to `http://localhost:8080/swagger-ui.html`
3. **Explore endpoints** - Click on any endpoint to see details
4. **Try it out** - Click "Try it out" button
5. **Fill parameters** - Enter required values
6. **Execute** - Click "Execute" to send the request
7. **View response** - See the response below

---

## 📦 Response Format

All successful responses follow this structure:

```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2024-11-04T15:30:45"
}
```

All error responses follow this structure:

```json
{
  "success": false,
  "message": "Error description",
  "error": "Error type",
  "status": 400,
  "path": "/api/endpoint",
  "timestamp": "2024-11-04T15:30:45",
  "validationErrors": [ ... ] // Only for validation errors
}
```

---

## 🔗 Useful Links

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/api/health
- **API Version**: http://localhost:8080/api/version

---

## 💡 Tips

1. **Use Swagger UI** for interactive testing
2. **Check validation errors** in the response for details
3. **Use pagination** for large datasets
4. **Sort and filter** to find specific data
5. **Check HTTP status codes** to understand response type

---

*Last updated: November 2025*