# Upload API Documentation

Base URL: `/upload`
Authentication: Required (`ROLE_ADMIN`)

## 1. Upload Image

Upload an image file to Cloudinary.

- **URL**: `/upload/image`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`

### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `file` | File | Yes | The image file to upload. Supported formats: jpg, jpeg, png, gif, webp. Max size: 10MB. |

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Image uploaded successfully",
    "data": {
        "url": "https://res.cloudinary.com/...",
        "publicId": "course-thumbnails/...",
        "format": "jpg",
        "bytes": 12345,
        "duration": null
    }
}
```

### Error Responses

- **400 Bad Request**: File is empty, too large, or invalid format.
- **401 Unauthorized**: User is not logged in.
- **403 Forbidden**: User is not an admin.

---

## 2. Upload Video

Upload a video file to Cloudinary.

- **URL**: `/upload/video`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`

### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `file` | File | Yes | The video file to upload. Supported formats: mp4, avi, mov, wmv, flv, mkv, webm. Max size: 500MB. |

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Video uploaded successfully",
    "data": {
        "url": "https://res.cloudinary.com/...",
        "publicId": "course-videos/...",
        "format": "mp4",
        "bytes": 10485760,
        "duration": 120
    }
}
```

### Error Responses

- **400 Bad Request**: File is empty, too large, or invalid format.
- **401 Unauthorized**: User is not logged in.
- **403 Forbidden**: User is not an admin.
