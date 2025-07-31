# AssetCare360 API Documentation

## Authentication

### POST /api/auth/login
Authenticate a user with employeeId, email, and password.

**Request Body:**
```json
{
  "employeeId": "string",
  "email": "string",
  "password": "string"
}
```

**Responses:**
- `200 OK`: Returns user info (JSON, omits password)
- `400 Bad Request`: Missing required fields
- `401 Unauthorized`: Invalid credentials or email mismatch
- `500 Internal Server Error`: Database error

---

## User CRUD (Auto-generated)

### GET /users
List all users.

**Responses:**
- `200 OK`: Array of users
- `500 Internal Server Error`: Database error

### GET /users/{id}
Get a user by ID.

**Responses:**
- `200 OK`: User object
- `404 Not Found`: User not found
- `400 Bad Request`: Invalid user ID
- `500 Internal Server Error`: Database error

### POST /users
Create a new user.

**Request Body:**
```json
{
  "employeeId": "string",
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string"
}
```

**Responses:**
- `201 Created`: Created user object
- `400 Bad Request`: Invalid JSON
- `500 Internal Server Error`: Database error or failed to save

### PUT /users/{id}
Update an existing user by ID.

**Request Body:**
```json
{
  "employeeId": "string",
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string"
}
```

**Responses:**
- `200 OK`: Updated user object
- `404 Not Found`: User not found
- `400 Bad Request`: Invalid ID
- `500 Internal Server Error`: Database error or failed to update

### DELETE /users/{id}
Delete a user by ID.

**Responses:**
- `204 No Content`: User deleted
- `404 Not Found`: User not found
- `400 Bad Request`: Invalid ID
- `500 Internal Server Error`: Database error or failed to delete

---

## Notes
- Not all CRUD operations may be allowed for every authenticated user. Role-based access control is recommended for production.
- All responses are JSON.
- More models can be added and will automatically get CRUD endpoints via the generic controller.
