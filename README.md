# MinIO File Upload Service

Backend service được xây dựng bằng Java Spring Boot để upload và quản lý tài liệu sử dụng MinIO như cloud storage.

## Tính năng

- Upload file thông qua REST API với form-data
- Download file từ MinIO
- Xóa file
- Lấy thông tin file
- Tự động tạo bucket nếu chưa tồn tại
- Validate file size và type
- Generate unique filename

## Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- MinIO Server

## Cài đặt và chạy

### 1. Cài đặt MinIO Server

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v ~/minio/data:/data \
  minio/minio server /data --console-address ":9001"
```

hoặc mở Docker và chạy
```
docker-compose up -d
```

### 2. Build và chạy ứng dụng

```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn spring-boot:run
```

Ứng dụng sẽ chạy trên port 8080.

## API Endpoints

### Upload File
File upload được lưu ở đây /minio/data
```
POST /api/files/upload
Content-Type: multipart/form-data

Body: file (multipart file)
```

### Download File
```
GET /api/files/download/{fileName}
```

### Get File Info
```
GET /api/files/info/{fileName}
```

### Delete File
```
DELETE /api/files/delete/{fileName}
```

### Health Check
```
GET /api/files/health
```

## Cấu hình

Cấu hình MinIO trong `src/main/resources/application.yml`:

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: documents
  secure: false
```

## Ví dụ sử dụng

### Upload file với curl
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/your/file.pdf"
```

### Upload file với JavaScript
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('http://localhost:8080/api/files/upload', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

## Response Format

### Upload Success
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "fileName": "uuid-generated-filename.pdf",
  "originalName": "document.pdf",
  "size": 1024000,
  "contentType": "application/pdf"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message"
}
```
