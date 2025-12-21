# Course API Documentation

Base URL: `/courses`

## 1. Create Course

Create a new course with optional thumbnail and intro video uploads.

- **URL**: `/courses`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Authentication**: Required (`ROLE_ADMIN`)

### Request Parts

| Part Name | Type | Required | Description |
|-----------|------|----------|-------------|
| `thumbnail` | File | No | The course thumbnail image (jpg, png, etc.). |
| `introVideo` | File | No | The course introduction video (mp4, etc.). |
| `title` | String | Yes | Course title. |
| `description` | String | No | Course description. |
| `price` | Number | Yes | Course price (must be positive). |
| `category` | String | No | Course category. |
| `level` | String | No | Course level (BEGINNER, INTERMEDIATE, ADVANCED). |
| `duration` | Integer | No | Duration in minutes. |
| `isPublished` | Boolean | No | Whether the course is published. |
| `courseType` | String | No | ONLINE or OFFLINE. |
| `registrationStartDate` | Date | No | (Offline only) YYYY-MM-DD. |
| `registrationEndDate` | Date | No | (Offline only) YYYY-MM-DD. |
| `courseStartDate` | Date | No | (Offline only) YYYY-MM-DD. |
| `courseEndDate` | Date | No | (Offline only) YYYY-MM-DD. |
| `maxStudents` | Integer | No | (Offline only) Maximum number of students. |
| `location` | String | No | (Offline only) Location name. |
| `address` | String | No | (Offline only) Full address. |

### Example Request (cURL)

```bash
curl -X POST http://localhost:8080/courses \
  -H "Authorization: Bearer <token>" \
  -F "thumbnail=@/path/to/image.jpg" \
  -F "introVideo=@/path/to/video.mp4" \
  -F "title=Java Programming Masterclass" \
  -F "price=49.99" \
  -F "level=BEGINNER" \
  -F "category=Programming"
```

### Success Response (201 Created)

```json
{
    "success": true,
    "message": "Course created successfully",
    "data": {
        "id": "12345678",
        "title": "Java Programming Masterclass",
        "thumbnail": "https://res.cloudinary.com/.../image.jpg",
        "introVideoUrl": "https://res.cloudinary.com/.../video.mp4",
        ...
    }
}
```

---

## 2. Update Course

Update an existing course. You can upload new files to replace existing ones.

- **URL**: `/courses/{id}`
- **Method**: `PUT`
- **Content-Type**: `multipart/form-data`
- **Authentication**: Required (`ROLE_ADMIN`)

### Request Parts

Same as Create Course. All fields are optional; provide only what needs to be updated.
**Note**: You do NOT need to re-upload `thumbnail` or `introVideo` if they haven't changed. If you omit them, the existing files will be preserved.

### Example Request (cURL)

```bash
curl -X PUT http://localhost:8080/courses/courseId123 \
  -H "Authorization: Bearer <token>" \
  -F "thumbnail=@/path/to/new-image.jpg" \
  -F "price=59.99"
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Course updated successfully",
    "data": { ... }
}
```

---

## 3. Get Featured Courses for Home Page

Get up to 3 featured courses to display on the home page.

- **URL**: `/courses/home`
- **Method**: `GET`
- **Authentication**: Not Required (Public Access)

### Description

This endpoint returns a maximum of 3 courses that have been marked as featured (`isShowHome = true`). The courses are sorted by their last update time in descending order (most recently updated first).

### Example Request (cURL)

```bash
curl -X GET http://localhost:8001/api/courses/home
```

### Success Response (200 OK)

```json
{
    "success": true,
    "message": "Success",
    "data": [
        {
            "id": "wqyXT6ZL",
            "title": "Đầu tư cơ bản",
            "description": "Một khóa học đầu tư cơ bản dành cho người mới bắt đầu",
            "price": 1000000.00,
            "thumbnail": "https://res.cloudinary.com/...",
            "category": "Doanh nghiệp",
            "isPublished": true,
            "isShowHome": true,
            "introVideoUrl": null,
            "averageRating": 0.0,
            "reviewCount": 0,
            "lessonCount": 0,
            "enrollmentCount": 0,
            "createdAt": "2025-12-16T10:57:00.607056",
            "updatedAt": "2025-12-16T10:57:00.607056",
            "isEnrolled": false
        }
    ]
}
```

### Notes

- **Public Access**: This endpoint does not require authentication and can be accessed by anyone.
- **Maximum Results**: Returns at most 3 courses.
- **Filtering**: Only courses with `isShowHome = true` are returned.
- **Sorting**: Courses are ordered by `updatedAt` in descending order.
- **Admin Control**: Admins can mark courses as featured by setting `isShowHome = true` when creating or updating courses. The system enforces a maximum of 3 featured courses at any time.
