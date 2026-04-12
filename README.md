# Convert File — PDF to DOCX

Ứng dụng web chuyển đổi file PDF sang DOCX, xử lý bất đồng bộ qua RabbitMQ với nhiều worker song song.

---

## Kiến trúc

```
Client (JSP)
    │
    ▼
Servlet (upload PDF)
    │  đẩy job vào queue
    ▼
RabbitMQ Queue
    │  worker lấy job
    ▼
Worker (PDFBox → POI OOXML)
    │  upload kết quả
    ▼
Cloudinary (lưu file)
    │  cập nhật trạng thái
    ▼
MySQL (conversions table)
```

File được upload lên Cloudinary, job chuyển đổi được đẩy vào RabbitMQ. Các worker độc lập lấy job, xử lý, rồi cập nhật trạng thái về database.

---

## Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17 · Jakarta Servlet · JSP |
| Build | Maven · WAR |
| Queue | RabbitMQ (amqp-client 5.18) |
| PDF xử lý | Apache PDFBox 2.0 |
| DOCX tạo file | Apache POI OOXML 5.2 |
| Lưu trữ file | Cloudinary |
| Database | MySQL 8 · HikariCP |
| Auth | BCrypt (jBCrypt) |
| Logging | Logback · SLF4J |

---

## Database

2 bảng chính:

```sql
users       -- tài khoản đăng nhập
conversions -- lịch sử chuyển đổi, trạng thái job
```

Trạng thái job: `UPLOADED` → `PENDING` → `PROCESSING` → `COMPLETED` / `FAILED`

---

## Chạy local

**Yêu cầu:** Java 17+, Maven, MySQL 8, RabbitMQ, Tomcat 10+

```bash
# 1. Clone repo
git clone https://github.com/juffww/convert_file.git
cd convert_file

# 2. Tạo database
mysql -u root -p < convert_file.sql

# 3. Cấu hình kết nối
# Sửa thông tin DB, RabbitMQ, Cloudinary trong file config tương ứng

# 4. Build
mvn clean package

# 5. Deploy WAR lên Tomcat
cp target/ConvertFile.war $TOMCAT_HOME/webapps/
```

Ứng dụng chạy tại `http://localhost:8080/ConvertFile`

---

## Luồng xử lý

```
1. User đăng nhập → upload file PDF
2. Server lưu file lên Cloudinary → tạo record conversions (status: UPLOADED)
3. Đẩy job_id vào RabbitMQ queue → cập nhật status = PENDING
4. Worker nhận job → tải PDF từ Cloudinary → chuyển đổi sang DOCX (status: PROCESSING)
5. Upload DOCX lên Cloudinary → cập nhật output_url, status = COMPLETED
6. User tải file DOCX về
```

---

## Tính năng

- Đăng ký / đăng nhập tài khoản
- Upload PDF và theo dõi trạng thái xử lý realtime
- Xử lý bất đồng bộ qua RabbitMQ — nhiều file cùng lúc không bị chặn
- Nhiều worker chạy song song, tự động chia tải
- Tải file DOCX kết quả sau khi xử lý xong
- Lịch sử các lần chuyển đổi
