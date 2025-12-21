# News API Documentation

Base URL: `/news`

## Overview

The News API provides endpoints to manage news items that can be displayed on the homepage. Administrators can create, update, and delete news, while all users (including unauthenticated) can view published news.

**Key Features:**
- Maximum 8 news items can be marked as featured (`isShowHome = true`)
- Public access to all GET endpoints
- Admin-only access for create, update, and delete operations
- Thumbnail upload support (multipart/form-data)

---

## 1. Create News

Create a new news item with optional thumbnail upload.

- **URL**: `/news`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Authentication**: Required (`ROLE_ADMIN`)

### Request Parts

| Part Name | Type | Required | Description |
|-----------|------|----------|-------------|
| `thumbnail` | File | No | The news thumbnail image (jpg, png, etc.). |
| `title` | String | Yes | News title (max 200 characters). |
| `description` | String | No | News description (TEXT). |
| `thumbnailUrl` | String | No | External thumbnail URL (if not uploading file). |
| `isPublished` | Boolean | No | Whether the news is published (default: false). |
| `isShowHome` | Boolean | No | Display on homepage (default: false, max 8 total). |

### Example Request (cURL)

```bash
curl -X POST http://localhost:8001/api/news \
  -H "Authorization: Bearer <admin-token>" \
  -F "thumbnail=@/path/to/image.jpg" \
  -F "title=Breaking News: New Course Launch" \
  -F "description=We are excited to announce..." \
  -F "isPublished=true" \
  -F "isShowHome=true"
```

### Success Response (201 Created)

```json
{
    "success": true,
    "message": "News created successfully",
    "data": {
        "id": "aBcD1234",
        "title": "Breaking News: New Course Launch",
        "description": "We are excited to announce...",
        "thumbnail": "https://res.cloudinary.com/.../image.jpg",
        "isPublished": true,
        "isShowHome": true,
        "createdAt": "2025-12-17T14:30:00",
        "updatedAt": "2025-12-17T14:30:00"
    }
}
```

### Error Responses

- **400 Bad Request**: Validation error or max 8 featured news limit reached
- **401 Unauthorized**: User is not logged in
- **403 Forbidden**: User is not an admin

---

## 2. Update News

Update an existing news item.

- **URL**: `/news/{id}`
- **Method**: `PUT`
- **Content-Type**: `multipart/form-data`
- **Authentication**: Required (`ROLE_ADMIN`)

### Request Parts

Same as Create News. All fields are optional; provide only what needs to be updated.

### Example Request (cURL)

```bash
curl -X PUT http://localhost:8001/api/news/aBcD1234 \
  -H "Authorization: Bearer <admin-token>" \
  -F "title=Updated News Title" \
  -F "isPublished=false"
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "News updated successfully",
    "data": { ... }
}
```

---

## 3. Delete News

Delete a news item.

- **URL**: `/news/{id}`
- **Method**: `DELETE`
- **Authentication**: Required (`ROLE_ADMIN`)

### Example Request (cURL)

```bash
curl -X DELETE http://localhost:8001/api/news/aBcD1234 \
  -H "Authorization: Bearer <admin-token>"
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "News deleted successfully",
    "data": null
}
```

---

## 4. Get News by ID

Get a single news item by ID.

- **URL**: `/news/{id}`
- **Method**: `GET`
- **Authentication**: Not Required (Public Access)

### Example Request (cURL)

```bash
curl -X GET http://localhost:8001/api/news/aBcD1234
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Success",
    "data": {
        "id": "aBcD1234",
        "title": "Breaking News",
        "description": "...",
        "thumbnail": "https://...",
        "isPublished": true,
        "isShowHome": true,
        "createdAt": "2025-12-17T14:30:00",
        "updatedAt": "2025-12-17T14:30:00"
    }
}
```

---

## 5. Get All News

Get all news items with optional published filter.

- **URL**: `/news`
- **Method**: `GET`
- **Authentication**: Not Required (Public Access)

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `published` | Boolean | No | Filter by published status. If `true`, returns only published news. |

### Example Requests (cURL)

```bash
# Get all news
curl -X GET http://localhost:8001/api/news

# Get only published news
curl -X GET "http://localhost:8001/api/news?published=true"
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Success",
    "data": [
        {
            "id": "aBcD1234",
            "title": "News 1",
            ...
        },
        {
            "id": "eFgH5678",
            "title": "News 2",
            ...
        }
    ]
}
```

---

## 6. Get Featured News for Homepage

Get up to 8 featured news items for homepage display.

- **URL**: `/news/home`
- **Method**: `GET`
- **Authentication**: Not Required (Public Access)

### Description

This endpoint returns a maximum of 8 news items that have been marked as featured (`isShowHome = true`). The news items are sorted by their last update time in descending order (most recently updated first).

### Example Request (cURL)

```bash
curl -X GET http://localhost:8001/api/news/home
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Success",
    "data": [
        {
            "id": "aBcD1234",
            "title": "Featured News 1",
            "description": "Important announcement",
            "thumbnail": "https://res.cloudinary.com/...",
            "isPublished": true,
            "isShowHome": true,
            "createdAt": "2025-12-17T14:30:00",
            "updatedAt": "2025-12-17T14:30:00"
        },
        {
            "id": "eFgH5678",
            "title": "Featured News 2",
            ...
        }
    ]
}
```

### Notes

- **Public Access**: This endpoint does not require authentication
- **Maximum Results**: Returns at most 8 news items
- **Filtering**: Only news with `isShowHome = true` are returned
- **Sorting**: News are ordered by `updatedAt` in descending order
- **Admin Control**: Admins can mark news as featured by setting `isShowHome = true`. The system enforces a maximum of 8 featured news at any time.

---

## Validation Rules

1. **Title**: Required, max 200 characters
2. **Featured News Limit**: Maximum 8 news items can have `isShowHome = true`
3. **Thumbnail**: Optional, can be uploaded as file or provided as URL
4. **Published Status**: Defaults to `false` if not specified

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Created successfully |
| 400 | Bad Request (validation error or limit exceeded) |
| 401 | Unauthorized (authentication required) |
| 403 | Forbidden (admin role required) |
| 404 | Not Found (news ID doesn't exist) |
