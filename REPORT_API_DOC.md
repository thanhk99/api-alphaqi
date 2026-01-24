# Tài liệu API Báo cáo (Reports)

Tài liệu này mô tả các API liên quan đến hệ thống báo cáo, bao gồm các tính năng tìm kiếm, lọc và quản lý dành cho Admin.

## Thông tin chung
- **Base Path**: `/reports`
- **Cấu trúc trả về**: `ApiResponse<T>`

---

## 1. Lấy danh sách loại báo cáo
Lấy danh sách các loại báo cáo hiện có trong hệ thống, bao gồm thông tin phân cấp (Cha - Con).

- **Endpoint**: `GET /reports/types`
- **Quyền hạn**: Mọi người dùng.
- **Trả về**: `ApiResponse<List<Map<String, Object>>>`
  - Ví dụ item (Cha): `{ "code": "MACRO", "displayName": "Báo cáo vĩ mô", "parentCode": null, "isParent": true }`
  - Ví dụ item (Con): `{ "code": "MACRO_TOPICAL", "displayName": "Báo cáo chuyên đề Vĩ mô", "parentCode": "MACRO", "isParent": false }`

---

## 2. Lấy danh sách báo cáo
Lấy danh sách báo cáo có hỗ trợ lọc theo loại, tìm kiếm theo tiêu đề và phân trang. 
**Lưu ý**: Nếu lọc theo loại Cha, hệ thống sẽ tự động trả về báo cáo của tất cả loại Con thuộc Cha đó.

- **Endpoint**: `GET /reports`
- **Quyền hạn**: Mọi người dùng.
- **Tham số truy vấn**:
  - `type` (optional): Loại báo cáo (ví dụ: `MACRO`, `MACRO_TOPICAL`, ...).
  - `search` (optional): Tìm kiếm theo tiêu đề (không phân biệt hoa thường).
  - `page` (optional, default: 0): Số trang.
  - `size` (optional, default: 10): Số bản ghi mỗi trang.
- **Trả về**: `ApiResponse<ReportPagedResponse>`
  - `reports`: Đối tượng `Page` chứa danh sách `ReportResponse`.
  - `latestUpdatedAt`: Thời gian cập nhật mới nhất của loại báo cáo đang lọc.

---

## 3. Lấy chi tiết báo cáo
- **Endpoint**: `GET /reports/{id}`
- **Ghi chú**: Tham số `{id}` phải là kiểu số (numeric).
- **Quyền hạn**: Mọi người dùng.
- **Trả về**: `ApiResponse<ReportResponse>`

---

## 4. Quản lý Báo cáo (Admin only)

### Tạo báo cáo mới
- **Endpoint**: `POST /reports`
- **Quyền hạn**: `ROLE_ADMIN`
- **Content-Type**: `multipart/form-data`
- **Tham số**:
  - `title` (text, bắt buộc): Tiêu đề báo cáo.
  - `description` (text, bắt buộc): Mô tả chi tiết.
  - `type` (text, bắt buộc): Loại báo cáo (ví dụ: `ASSET_ALLOCATION`, `MACRO_TOPICAL`, ...). Xem danh sách đầy đủ ở mục 5.
  - `file` (file, tùy chọn): Tệp PDF đính kèm hoặc hình ảnh.
- **Trả về**: `ApiResponse<ReportResponse>`
- **Ví dụ request** (sử dụng FormData):
  ```
  title: "Báo cáo phân bổ tài sản Q1 2024"
  description: "Phân tích chi tiết về phân bổ tài sản..."
  type: "ASSET_ALLOCATION"
  file: [File object]
  ```

### Cập nhật báo cáo
- **Endpoint**: `PUT /reports/{id}`
- **Quyền hạn**: `ROLE_ADMIN`
- **Content-Type**: `multipart/form-data`
- **Tham số**: Tương tự như tạo mới (tất cả đều bắt buộc).
  - `title` (text, bắt buộc): Tiêu đề báo cáo.
  - `description` (text, bắt buộc): Mô tả chi tiết.
  - `type` (text, bắt buộc): Loại báo cáo.
  - `file` (file, tùy chọn): Tệp PDF đính kèm hoặc hình ảnh mới (nếu muốn thay đổi).
- **Trả về**: `ApiResponse<ReportResponse>`
- **Lưu ý**: Nếu không gửi `file`, file cũ sẽ được giữ nguyên.

### Xóa báo cáo
- **Endpoint**: `DELETE /reports/{id}`
- **Quyền hạn**: `ROLE_ADMIN`
- **Trả về**: `ApiResponse<Void>`

---

---

## 5. Hướng dẫn chọn Loại báo cáo khi tạo mới

Khi tạo hoặc cập nhật báo cáo qua API `POST /reports` hoặc `PUT /reports/{id}`, bạn cần truyền giá trị `type` tương ứng với mã (Code) dưới đây.

### Danh sách các loại báo cáo hỗ trợ phân cấp:

| Nhóm (Cha) | Loại chi tiết (Con) - **Nên dùng code này** | Diễn giải |
| :--- | :--- | :--- |
| **MACRO** | `MACRO_MONEY_MARKET_BOND` | Thị trường Tiền tệ & Trái phiếu |
| | `MACRO_TOPICAL` | Chuyên đề Vĩ mô |
| **COMPANY_INDUSTRY** | `COMPANY` | Báo cáo Công ty |
| | `SECTOR` | Báo cáo ngành |
| **ASSET_MANAGEMENT** | `ASSET_ALLOCATION` | Phân bổ tài sản |
| | `WEALTH_MANAGEMENT_TOPICAL` | Chuyên đề Quản lý tài sản |

### Các loại báo cáo độc lập (Không phân cấp):
- `INVESTMENT_STRATEGY`: Báo cáo chiến lược đầu tư
- `CIO_REPORT`: Báo cáo CIO
- `MACRO`: Báo cáo vĩ mô chung (Nếu không thuộc 2 loại con trên)
- `COMPANY_INDUSTRY`: Báo cáo công ty, ngành chung
- `ASSET_MANAGEMENT`: Báo cáo quản lý tài sản chung

### Lưu ý quan trọng:
1. **Khuyến khích chọn loại Con**: Khi bạn tạo báo cáo với loại Con (ví dụ `MACRO_TOPICAL`), báo cáo đó sẽ **tự động** xuất hiện cả khi người dùng lọc theo loại Cha (`MACRO`).
2. **Loại Cha**: Chỉ chọn loại Cha (ví dụ `MACRO`) nếu báo cáo đó mang tính tổng quát và không thuộc bất kỳ loại Con nào đã định nghĩa.

---

## Cấu trúc dữ liệu `ReportResponse`
```json
{
  "id": 1,
  "title": "Báo cáo doanh thu tháng 1",
  "description": "Chi tiết doanh thu...",
  "type": "MACRO_TOPICAL",
  "typeDisplayName": "Báo cáo chuyên đề Vĩ mô",
  "parentType": "MACRO",
  "parentTypeDisplayName": "Báo cáo vĩ mô",
  "pdUrl": "https://cloudinary.com/...",
  "createdAt": "2024-01-23T10:00:00",
  "updatedAt": "2024-01-23T15:00:00"
}
```
