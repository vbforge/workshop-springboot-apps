# 📦 Postman Collection Overview

## 🚀 Key Features

### 🔐 Automated Variables

The collection automatically manages the following variables:

* `jwt_token` — Librarian’s JWT token (set after login)
* `user_jwt_token` — User’s JWT token (set after login)
* `librarian_id`, `user_id`, `book_id` — Automatically set after signup/login

---

### 🔄 Sequential Flow

Requests are organized in a logical execution order:

1. **Auth**

    * Signup/Login for librarian and user

2. **Librarian Actions**

    * Add books
    * Update roles
    * Manage resources

3. **User Actions**

    * Borrow books
    * Return books
    * View records

4. **Error Scenarios**

    * Invalid inputs
    * Unauthorized access

---

### ⚙️ Pre-configured Scripts

* Automatically extract:

    * `jwt_token`
    * `user_id`
    * `book_id`
* Store variables for reuse in subsequent requests

---

## 📌 How to Use

### 1. Import Collection

* Import the JSON file into Postman

### 2. Run the Collection

* Execute the collection runner
* Requests will run in sequence
* Variables (`jwt_token`, `user_id`, etc.) are set automatically

### 3. Verify Responses

* ✅ `200 OK` — Successful requests
* ⚠️ `4xx` — Client errors

    * `400 Bad Request`
    * `403 Forbidden`

---

## 🎯 Expected Workflow

### 🔑 Signup / Login

* Create librarian and user accounts
* JWT tokens are generated and stored automatically

---

### 📚 Librarian Actions

* Add a book
* Update book details
* Retrieve lists of books and users

---

### 👤 User Actions

* Borrow a book
* Return a book
* View borrowing records

---

### ❗ Error Scenarios

* Validate handling of:

    * Invalid book IDs
    * Unauthorized access attempts

---

