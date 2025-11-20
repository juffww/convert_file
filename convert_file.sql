-- Tạo database
CREATE DATABASE IF NOT EXISTS pdf_converter_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pdf_converter_db;

-- Tạo bảng users trước
CREATE TABLE IF NOT EXISTS users (
  id int NOT NULL AUTO_INCREMENT,
  username varchar(50) NOT NULL,
  password varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo bảng conversions sau
CREATE TABLE IF NOT EXISTS conversions (
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  input_url varchar(500) NOT NULL,
  input_public_id varchar(100) DEFAULT NULL,
  input_filename varchar(255) NOT NULL,
  output_url varchar(500) DEFAULT NULL,
  output_public_idgg varchar(100) DEFAULT NULL,
  status enum('UPLOADED','PENDING','PROCESSING','COMPLETED','FAILED') DEFAULT 'UPLOADED',
  error_message text,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm khóa ngoại sau khi cả hai bảng đã được tạo
ALTER TABLE conversions 
ADD CONSTRAINT fk_user 
FOREIGN KEY (user_id) 
REFERENCES users (id) 
ON DELETE CASCADE;

-- Chèn dữ liệu mẫu
INSERT INTO users (username, password) VALUES 
('admin', '123456'),
('sinhvien', '123456');