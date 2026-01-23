# Tài liệu API Quản lý Người dùng (Cập nhật Phân trang mới)

Tài liệu này mô tả các API quản lý người dùng sau khi đã được đơn giản hóa (trả về `Page<UserResponse>` trực tiếp).

## Thông tin chung
- **Base Path**: `/users`
- **Cấu trúc trả về**: `ApiResponse<T>`
- **Cơ chế Phân trang**: Sử dụng các tham số `page`, `size` và `sort` của Spring Data.

---

## 1. Lấy danh sách tất cả người dùng (Admin)
Lấy danh sách người dùng có hỗ trợ tìm kiếm và phân trang.

- **Endpoint**: `GET /users`
- **Quyền hạn**: `ROLE_ADMIN`
- **Tham số truy vấn**:
  - `search` (optional): Tìm kiếm theo tên (Full Name) hoặc Tên đăng nhập (Username) (không phân biệt hoa thường).
  - `page` (optional, default: 0): Số trang.
  - `size` (optional, default: 10): Số bản ghi mỗi trang.
- **Trả về**: `ApiResponse<Page<UserResponse>>`

---

## 2. Lấy danh sách người dùng đang hoạt động (Admin)
- **Endpoint**: `GET /users/active`
- **Quyền hạn**: `ROLE_ADMIN`
- **Tham số truy vấn**: Tương tự như lấy tất cả người dùng.
- **Trả về**: `ApiResponse<Page<UserResponse>>`

---

## 3. Lấy danh sách người dùng đã xóa (LOCKED) (Admin)
- **Endpoint**: `GET /users/deleted`
- **Quyền hạn**: `ROLE_ADMIN`
- **Tham số truy vấn**: Tương tự như lấy tất cả người dùng.
- **Trả về**: `ApiResponse<Page<UserResponse>>`

---

## 4. Lấy chi tiết người dùng
- **Endpoint**: `GET /users/{username}`
- **Quyền hạn**: `ROLE_ADMIN` hoặc chính người dùng đó.
- **Trả về**: `ApiResponse<UserResponse>`

---

## 5. Quản lý trạng thái (Admin)

### Khóa người dùng
- **Endpoint**: `PUT /users/{id}/lock`
- **Trả về**: `ApiResponse<UserResponse>`

### Mở khóa người dùng
- **Endpoint**: `PUT /users/{id}/unlock`
- **Trả về**: `ApiResponse<UserResponse>`

### Xóa người dùng (Soft Delete)
- **Endpoint**: `DELETE /users/{id}`
- **Trả về**: `ApiResponse<UserResponse>`

### Khôi phục người dùng
- **Endpoint**: `PUT /users/{id}/restore`
- **Trả về**: `ApiResponse<UserResponse>`

---

## 6. Hồ sơ cá nhân (User/Admin)

### Lấy thông tin cá nhân hiện tại
- **Endpoint**: `GET /users/me`
- **Trả về**: `ApiResponse<UserResponse>`

### Đổi mật khẩu
- **Endpoint**: `PUT /users/me/password`
- **Body**: `ChangePasswordRequest`
- **Trả về**: `ApiResponse<String>`

### Cập nhật hồ sơ
- **Endpoint**: `PUT /users/me/profile`
- **Body**: `UpdateProfileRequest`
- **Trả về**: `ApiResponse<UserResponse>`

### Cập nhật Avatar (Chọn ảnh mặc định)
- **Endpoint**: `PUT /users/me/avatar`
- **Body**: `{ "avatarUrl": "string" }`
- **Trả về**: `ApiResponse<UserResponse>`

---

## Cấu trúc dữ liệu `UserResponse`
```json
{
  "id": "string",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "phoneNumber": "string",
  "avatar": "string",
  "membershipLevel": "string",
  "status": "string (ACTIVE/LOCKED)",
  "createdAt": "ISO8601",
  "updatedAt": "ISO8601",
  "deletedAt": "ISO8601"
}
```
