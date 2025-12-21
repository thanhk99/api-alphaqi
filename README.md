# Financial Investment Course Platform - Backend API

## Tổng Quan

Hệ thống backend API cho nền tảng bán khóa học đầu tư tài chính, được xây dựng với Spring Boot 3.5.8, PostgreSQL, và JWT authentication.

## Công Nghệ Sử Dụng

- **Framework**: Spring Boot 3.5.8
- **Java**: 21
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT (Access Token + Refresh Token)
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven
- **Password Encryption**: BCrypt

## Tính Năng Chính

### Authentication & Authorization
- ✅ Đăng ký user mới
- ✅ Đăng nhập riêng biệt cho User và Admin
- ✅ JWT dual-token system (Access Token 15 phút, Refresh Token 7 ngày)
- ✅ Refresh token để gia hạn access token
- ✅ Logout (xóa refresh token)
- ✅ Phân quyền ROLE_USER và ROLE_ADMIN

### User Management
- ✅ Tách bảng Admin và User riêng biệt
- ✅ User có 3 cấp độ membership: NORMAL, VIP, SUPER_VIP
- ✅ Trạng thái tài khoản: ACTIVE, LOCKED
- ✅ Admin có thể khóa/mở khóa user
- ✅ Admin có thể nâng cấp membership cho user

### Course Management
- ✅ CRUD khóa học (Admin only)
- ✅ Tìm kiếm khóa học (full-text search)
- ✅ Lọc khóa học theo category và level
- ✅ Quản lý lessons cho mỗi khóa học
- ✅ Publish/unpublish khóa học

### Enrollment
- ✅ Đăng ký khóa học
- ✅ Theo dõi tiến độ học (progress %)
- ✅ Tự động đánh dấu hoàn thành khi progress = 100%
- ✅ Hủy enrollment

### ID Generation
- ✅ Tất cả entities sử dụng ID 8 ký tự alphanumeric tự sinh

## Cấu Trúc Database

### Tables
1. **admins** - Quản trị viên
2. **users** - Người dùng với membership levels
3. **refresh_tokens** - Lưu refresh tokens
4. **courses** - Khóa học
5. **lessons** - Bài học trong khóa học
6. **enrollments** - Đăng ký khóa học
7. **payments** - Thanh toán (cấu trúc cơ bản)

## Cài Đặt và Chạy

### 1. Yêu Cầu
- Java 21
- PostgreSQL 12+
- Maven 3.6+

### 2. Cấu Hình Database

Tạo database PostgreSQL:
```sql
CREATE DATABASE course_platform;
```

Cập nhật file `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/course_platform
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Cấu Hình JWT Secret

Trong `application.properties`, thay đổi JWT secret key:
```properties
jwt.secret=YourSecretKeyHere_PleaseChangeThis_AtLeast256BitsLong
```

### 4. Build và Run

```bash
# Download dependencies
mvn clean install

# Run application
mvn spring-boot:run
```

Application sẽ chạy tại: `http://localhost:8080/api`

## API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Đăng ký user mới | No |
| POST | `/login/user` | User login | No |
| POST | `/login/admin` | Admin login | No |
| POST | `/refresh` | Refresh access token | No |
| POST | `/logout` | Logout | Yes |

### Courses (`/api/courses`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Lấy tất cả courses | No |
| GET | `/{id}` | Lấy course theo ID | No |
| GET | `/search?keyword=...` | Tìm kiếm courses | No |
| GET | `/filter?category=...&level=...` | Lọc courses | No |
| POST | `/` | Tạo course mới | Admin |
| PUT | `/{id}` | Cập nhật course | Admin |
| DELETE | `/{id}` | Xóa course | Admin |

### Lessons (`/api/lessons`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/course/{courseId}` | Lấy lessons của course | No |
| POST | `/course/{courseId}` | Tạo lesson mới | Admin |
| PUT | `/{id}` | Cập nhật lesson | Admin |
| DELETE | `/{id}` | Xóa lesson | Admin |

### Enrollments (`/api/enrollments`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Đăng ký course | User |
| GET | `/my` | Lấy enrollments của user | User |
| PUT | `/{id}/progress?progress=...` | Cập nhật tiến độ | User |
| DELETE | `/{id}` | Hủy enrollment | User |

### Users (`/api/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Lấy tất cả users | Admin |
| GET | `/{id}` | Lấy user theo ID | Admin/Owner |
| PUT | `/{id}/membership?level=...` | Nâng cấp membership | Admin |
| PUT | `/{id}/lock` | Khóa user | Admin |
| PUT | `/{id}/unlock` | Mở khóa user | Admin |

### Admins (`/api/admins`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Lấy tất cả admins | Admin |
| POST | `/` | Tạo admin mới | Admin |
| PUT | `/{id}` | Cập nhật admin | Admin |
| PUT | `/{id}/lock` | Khóa admin | Admin |
| PUT | `/{id}/unlock` | Mở khóa admin | Admin |

## Request/Response Examples

### 1. Register User

**Request:**
```json
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "phoneNumber": "0123456789"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "userId": "aB3dE5fG",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "membershipLevel": "NORMAL",
    "status": "ACTIVE"
  }
}
```

### 2. Login User

**Request:**
```json
POST /api/auth/login/user
{
  "username": "john_doe",
  "password": "SecurePass123"
}
```

**Response:** (Same as register)

### 3. Create Course (Admin)

**Request:**
```json
POST /api/courses
Authorization: Bearer <access_token>

{
  "title": "Đầu tư chứng khoán cơ bản",
  "description": "Khóa học dành cho người mới bắt đầu",
  "price": 999000,
  "category": "Stock Trading",
  "level": "BEGINNER",
  "duration": 120,
  "isPublished": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "Course created successfully",
  "data": {
    "id": "xY9zA1bC",
    "title": "Đầu tư chứng khoán cơ bản",
    "description": "Khóa học dành cho người mới bắt đầu",
    "price": 999000,
    "category": "Stock Trading",
    "level": "BEGINNER",
    "duration": 120,
    "isPublished": true,
    "lessonCount": 0,
    "enrollmentCount": 0,
    "createdAt": "2025-12-02T14:30:00",
    "updatedAt": "2025-12-02T14:30:00"
  }
}
```

### 4. Enroll Course

**Request:**
```json
POST /api/enrollments
Authorization: Bearer <access_token>

{
  "courseId": "xY9zA1bC"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Enrolled successfully",
  "data": {
    "id": "pQ7rS2tU",
    "userId": "aB3dE5fG",
    "courseId": "xY9zA1bC",
    "status": "ACTIVE",
    "progress": 0.0,
    "enrolledAt": "2025-12-02T14:35:00"
  }
}
```

## Error Handling

Tất cả errors đều trả về format chuẩn:

```json
{
  "timestamp": "2025-12-02T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Course not found with id: 'invalid_id'",
  "path": "/api/courses/invalid_id"
}
```

### Common HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (invalid/expired token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `500` - Internal Server Error

## Security Notes

1. **Password Encryption**: Tất cả passwords được mã hóa bằng BCrypt
2. **JWT Tokens**: 
   - Access Token: 15 phút (cho API calls)
   - Refresh Token: 7 ngày (để gia hạn access token)
3. **Account Locking**: Admin có thể khóa accounts để prevent access
4. **Role-Based Access**: Endpoints được bảo vệ theo roles (USER/ADMIN)

## Troubleshooting

### Build Errors

Nếu gặp lỗi compile về validation annotations:
```bash
mvn clean install -U
```

### Database Connection Errors

Kiểm tra:
1. PostgreSQL đang chạy
2. Database đã được tạo
3. Username/password trong `application.properties` đúng
4. Port 5432 không bị block

### JWT Errors

Đảm bảo:
1. JWT secret key đủ dài (ít nhất 256 bits)
2. Token được gửi trong header: `Authorization: Bearer <token>`
3. Token chưa hết hạn

## Next Steps

- [ ] Tích hợp payment gateway (VNPay, MoMo, etc.)
- [ ] Thêm email verification
- [ ] Implement forgot password
- [ ] Add file upload cho course thumbnails
- [ ] Add video streaming cho lessons
- [ ] Implement rating & review system
- [ ] Add analytics dashboard

## License

Private project - All rights reserved
