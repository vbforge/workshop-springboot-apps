**📦 Error Response Examples:**

1. Resource Not Found (404)
```
{
  "success": false,
  "message": "Book not found with id: '999'",
  "error": "Resource Not Found",
  "status": 404,
  "path": "/api/books/999",
  "timestamp": "2024-11-04T15:30:45"
}

```

2. Validation Error (400)
```
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
    },
    {
      "field": "price",
      "message": "Price must be greater than 0",
      "rejectedValue": -10.0
    }
  ]
}
```

3. Duplicate Resource (409)
```
{
  "success": false,
  "message": "Book already exists with ISBN: '978-0-123456-78-9'",
  "error": "Duplicate Resource",
  "status": 409,
  "path": "/api/books",
  "timestamp": "2024-11-04T15:30:45"
}
```

---

## 🎯 HTTP Status Codes Used:

- **200 OK** - Successful GET, PUT
- **201 Created** - Successful POST
- **400 Bad Request** - Validation errors, invalid operations
- **404 Not Found** - Resource doesn't exist
- **409 Conflict** - Duplicate resource
- **500 Internal Server Error** - Unexpected errors

---