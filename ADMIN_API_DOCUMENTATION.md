# Admin API Documentation - Complete Guide

## Base URL
```
http://localhost:8001/api
```

---

### 2. Login Admin
**Endpoint:** `POST /auth/login/admin`  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "username": "adminUser",
  "password": "StrongP@ssw0rd"
}
```
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "userId": "eMSpMBkl",
    "username": "adminUser",
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": "ACTIVE"
  }
}
```
**Set-Cookie:** `refreshToken=a1b2c3d4...; HttpOnly; Path=/api/auth; Max-Age=604800`

---

### 3. Refresh Token
**Endpoint:** `POST /auth/refresh`  
**Headers:** `Cookie: refreshToken={refreshToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "newAccessToken...",
    "refreshToken": "newRefreshToken...",
    "userId": "eMSpMBkl",
    "username": "adminUser",
    "email": "admin@example.com",
    "role": "ADMIN",
    "status": "ACTIVE"
  }
}
```

---

### 4. Logout Admin
**Endpoint:** `POST /auth/logout`  
**Headers:** `Cookie: refreshToken={refreshToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```
**Effect:** Clears the refreshToken cookie and deletes token from database.

---

## Admin Management (Admin Only)

### 5. Get All Admins
**Endpoint:** `GET /admins`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "eMSpMBkl",
      "username": "adminUser",
      "email": "admin@example.com",
      "fullName": "Admin User",
      "phoneNumber": "0123456789",
      "status": "ACTIVE",
      "createdAt": "2025-12-03T12:00:00"
    }
  ]
}
```

---

### 6. Update Admin
**Endpoint:** `PUT /admins/{id}`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Request Body:**
```json
{
  "email": "newemail@example.com",
  "fullName": "Updated Name",
  "phoneNumber": "0987654321"
}
```
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Admin updated successfully",
  "data": {
    "id": "eMSpMBkl",
    "username": "adminUser",
    "email": "newemail@example.com",
    "fullName": "Updated Name",
    "phoneNumber": "0987654321",
    "status": "ACTIVE"
  }
}
```

---

### 7. Lock Admin
**Endpoint:** `PUT /admins/{id}/lock`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Admin locked successfully",
  "data": {
    "id": "eMSpMBkl",
    "username": "adminUser",
    "status": "LOCKED"
  }
}
```

---

### 8. Unlock Admin
**Endpoint:** `PUT /admins/{id}/unlock`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Admin unlocked successfully",
  "data": {
    "id": "eMSpMBkl",
    "username": "adminUser",
    "status": "ACTIVE"
  }
}
```

---

## Dashboard (Admin Only)

### 8.1. Get Dashboard Statistics
**Endpoint:** `GET /dashboard/stats`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalUsers": 150,
    "totalCourses": 12,
    "activeEnrollments": 45,
    "pendingReviews": 5
  }
}
```

---

## User Management (Admin Only)

### 9. Get All Users
**Endpoint:** `GET /users`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "user001",
      "username": "john",
      "email": "john@example.com",
      "membershipLevel": "BASIC",
      "status": "ACTIVE"
    }
  ]
}
```

---

## Course Management (Admin Only)

### 10. Create Course
**Endpoint:** `POST /courses`  
**Headers:** `Authorization: Bearer {accessToken}`  
**Request Body:** (See COURSE_API_DOCUMENTATION.md)

---

### 11. Update Course
**Endpoint:** `PUT /courses/{id}`  
**Headers:** `Authorization: Bearer {accessToken}`

---

### 12. Delete Course
**Endpoint:** `DELETE /courses/{id}`  
**Headers:** `Authorization: Bearer {accessToken}`

---

## Review Management (Admin Only)

### 13. Get All Reviews
**Endpoint:** `GET /reviews`  
**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "review123",
      "userId": "user001",
      "userName": "John Doe",
      "rating": 5,
      "comment": "Great course!",
      "createdAt": "2025-12-03T12:00:00"
    }
  ]
}
```

---

### 14. Delete Any Review
**Endpoint:** `DELETE /reviews/{id}`  
**Headers:** `Authorization: Bearer {accessToken}`

---

## Error Handling

All error responses follow this structure:
```json
{
  "timestamp": "2025-12-03T12:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/admins"
}
```

**Common Status Codes:**
- `400` – Bad Request (validation errors)
- `401` – Unauthorized (missing/invalid token)
- `403` – Forbidden (insufficient permissions)
- `404` – Not Found
- `409` – Conflict (duplicate username/email)
- `500` – Internal Server Error

---

## Testing with cURL

```bash
# 1. Create admin (no auth required)
curl -X POST http://localhost:8001/api/admins \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"admin123","fullName":"Admin User"}'

# 2. Login admin (stores refresh token cookie)
curl -i -c cookies.txt -X POST http://localhost:8001/api/auth/login/admin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 3. Refresh token using stored cookie
curl -b cookies.txt -X POST http://localhost:8001/api/auth/refresh

# 4. Get all admins (requires access token)
curl -X GET http://localhost:8001/api/admins \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 5. Logout
curl -b cookies.txt -X POST http://localhost:8001/api/auth/logout
```

---

## Notes

1. **Authentication Flow:**
   - Create admin via `POST /admins` (public endpoint)
   - Login via `POST /auth/login/admin` to get access token
   - Use access token in `Authorization: Bearer {token}` header for protected endpoints
   - Refresh token is stored as HttpOnly cookie automatically

2. **Security:**
   - Refresh tokens are HttpOnly cookies (XSS protection)
   - Access tokens expire in 15 minutes
   - Refresh tokens expire in 7 days
   - Use HTTPS in production

3. **CORS:**
   - Allowed origins: `localhost:3000`, `localhost:3001`, `localhost:5173`
   - Credentials (cookies) are allowed

4. **Role-Based Access:**
   - Most admin endpoints require `ROLE_ADMIN`
   - Creating admin is public (first-time setup)
   - Consider adding authorization checks for admin creation in production
