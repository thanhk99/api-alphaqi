# User Management API Documentation

## Base URL
```
http://localhost:8001/api
```

---

## Overview

This API provides comprehensive user management functionality with **soft delete** capabilities. Deleted users are not permanently removed from the database but marked with a `deletedAt` timestamp and can be restored.

---

## Endpoints

### 1. Get All Users
**Endpoint:** `GET /users`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "abc12345",
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "phoneNumber": "0123456789",
      "membershipLevel": "PREMIUM",
      "status": "ACTIVE",
      "createdAt": "2025-12-01T10:00:00",
      "updatedAt": "2025-12-02T15:30:00",
      "deletedAt": null
    }
  ]
}
```

**Note:** Returns all users including deleted ones. Use `/users/active` or `/users/deleted` for filtered lists.

---

### 2. Get Active Users Only
**Endpoint:** `GET /users/active`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "abc12345",
      "username": "john_doe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "phoneNumber": "0123456789",
      "membershipLevel": "PREMIUM",
      "status": "ACTIVE",
      "createdAt": "2025-12-01T10:00:00",
      "updatedAt": "2025-12-02T15:30:00",
      "deletedAt": null
    }
  ]
}
```

**Note:** Returns only users where `deletedAt` is `null`.

---

### 3. Get Deleted Users Only
**Endpoint:** `GET /users/deleted`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "xyz67890",
      "username": "jane_smith",
      "email": "jane@example.com",
      "fullName": "Jane Smith",
      "phoneNumber": "0987654321",
      "membershipLevel": "NORMAL",
      "status": "LOCKED",
      "createdAt": "2025-11-15T08:00:00",
      "updatedAt": "2025-12-03T12:00:00",
      "deletedAt": "2025-12-03T12:00:00"
    }
  ]
}
```

**Note:** Returns only users where `deletedAt` is not `null`.

---

### 4. Get User By ID
**Endpoint:** `GET /users/{id}`  
**Authorization:** Admin or the user themselves  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "abc12345",
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phoneNumber": "0123456789",
    "membershipLevel": "PREMIUM",
    "status": "ACTIVE",
    "createdAt": "2025-12-01T10:00:00",
    "updatedAt": "2025-12-02T15:30:00",
    "deletedAt": null
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2025-12-03T18:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: abc12345",
  "path": "/api/users/abc12345"
}
```

---

### 5. Upgrade Membership
**Endpoint:** `PUT /users/{id}/membership`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  
**Query Parameters:**
- `level` (required): `NORMAL`, `BASIC`, or `PREMIUM`

**Example Request:**
```
PUT /users/abc12345/membership?level=PREMIUM
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Membership upgraded successfully",
  "data": {
    "id": "abc12345",
    "username": "john_doe",
    "email": "john@example.com",
    "membershipLevel": "PREMIUM",
    "status": "ACTIVE"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-12-03T18:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid membership level: INVALID",
  "path": "/api/users/abc12345/membership"
}
```

---

### 6. Lock User
**Endpoint:** `PUT /users/{id}/lock`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User locked successfully",
  "data": {
    "id": "abc12345",
    "username": "john_doe",
    "status": "LOCKED"
  }
}
```

**Effect:** User cannot login until unlocked.

---

### 7. Unlock User
**Endpoint:** `PUT /users/{id}/unlock`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User unlocked successfully",
  "data": {
    "id": "abc12345",
    "username": "john_doe",
    "status": "ACTIVE"
  }
}
```

---

### 8. Delete User (Soft Delete)
**Endpoint:** `DELETE /users/{id}`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": {
    "id": "abc12345",
    "username": "john_doe",
    "email": "john@example.com",
    "status": "LOCKED",
    "deletedAt": "2025-12-03T18:30:00"
  }
}
```

**Effects:**
- Sets `deletedAt` to current timestamp
- Sets `status` to `LOCKED`
- User cannot login
- User data remains in database

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-12-03T18:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User is already deleted",
  "path": "/api/users/abc12345"
}
```

---

### 9. Restore User
**Endpoint:** `PUT /users/{id}/restore`  
**Authorization:** Admin only  
**Headers:** `Authorization: Bearer {accessToken}`  

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User restored successfully",
  "data": {
    "id": "abc12345",
    "username": "john_doe",
    "email": "john@example.com",
    "status": "ACTIVE",
    "deletedAt": null
  }
}
```

**Effects:**
- Sets `deletedAt` to `null`
- Sets `status` to `ACTIVE`
- User can login again

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-12-03T18:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User is not deleted",
  "path": "/api/users/abc12345/restore"
}
```

---

## Data Models

### UserResponse
```json
{
  "id": "string (8 chars)",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "phoneNumber": "string",
  "membershipLevel": "NORMAL | BASIC | PREMIUM",
  "status": "ACTIVE | LOCKED",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime",
  "deletedAt": "ISO 8601 datetime | null"
}
```

### Membership Levels
- `NORMAL`: Default level
- `BASIC`: Basic paid membership
- `PREMIUM`: Premium paid membership

### Account Status
- `ACTIVE`: User can login and use the system
- `LOCKED`: User cannot login (manually locked or soft deleted)

---

## Error Handling

All error responses follow this structure:
```json
{
  "timestamp": "ISO 8601 datetime",
  "status": "HTTP status code",
  "error": "Error type",
  "message": "Detailed error message",
  "path": "Request path"
}
```

**Common Status Codes:**
- `400` - Bad Request (validation errors, invalid state)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (user doesn't exist)
- `500` - Internal Server Error

---

## Testing with cURL

```bash
# Get all users (admin only)
curl -X GET http://localhost:8001/api/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get active users only
curl -X GET http://localhost:8001/api/users/active \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get deleted users only
curl -X GET http://localhost:8001/api/users/deleted \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get user by ID
curl -X GET http://localhost:8001/api/users/abc12345 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Upgrade membership
curl -X PUT "http://localhost:8001/api/users/abc12345/membership?level=PREMIUM" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Lock user
curl -X PUT http://localhost:8001/api/users/abc12345/lock \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Unlock user
curl -X PUT http://localhost:8001/api/users/abc12345/unlock \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Delete user (soft delete)
curl -X DELETE http://localhost:8001/api/users/abc12345 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Restore deleted user
curl -X PUT http://localhost:8001/api/users/abc12345/restore \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Notes

1. **Soft Delete vs Hard Delete:**
   - This API implements **soft delete** only
   - Deleted users are marked with `deletedAt` timestamp
   - Data is preserved and can be restored
   - No permanent data loss

2. **Security:**
   - All endpoints require authentication
   - Most endpoints require `ROLE_ADMIN`
   - Users can only view their own data (except admins)

3. **Best Practices:**
   - Use `/users/active` for normal user lists
   - Use `/users/deleted` to review deleted accounts
   - Restore users instead of creating duplicates
   - Lock users before deletion for safety

4. **Future Enhancements:**
   - Pagination for large user lists
   - Search and filter capabilities
   - Bulk operations
   - Hard delete after retention period
