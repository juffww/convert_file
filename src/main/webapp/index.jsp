<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="style.css">
    <title>Đăng nhập - PDF to DOCX Converter</title>
    <style>
        
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>Đăng nhập</h1>
        </div>
        
        <form class="login-form" action="auth" method="POST" onsubmit="return validateForm()">
            <div class="error-message" id="errorMessage"></div>
            
            <div class="form-group">
                <label for="username">Tên đăng nhập</label>
                <input 
                    type="text" 
                    id="username" 
                    name="username" 
                    placeholder="Nhập tên đăng nhập"
                    required
                    autocomplete="username"
                    value="sinhvien"
                >
            </div>

            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <input 
                    type="password" 
                    id="password" 
                    name="password" 
                    placeholder="Nhập mật khẩu"
                    required
                    autocomplete="current-password"
                    value="123456"
                >
            </div>


            <button type="submit" class="login-button">Đăng nhập</button>

            <div class="divider">
                <span>hoặc</span>
            </div>

            <div class="signup-link">
                Chưa có tài khoản? <a href="register.jsp">Đăng ký </a>
            </div>
        </form>
    </div>

    <script>
        function validateForm() {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const errorMessage = document.getElementById('errorMessage');

            if (username.length < 3) {
                showError('Tên đăng nhập phải có ít nhất 3 ký tự');
                return false;
            }

            if (password.length < 6) {
                showError('Mật khẩu phải có ít nhất 6 ký tự');
                return false;
            }

            return true;
        }

        function showError(message) {
            const errorMessage = document.getElementById('errorMessage');
            errorMessage.textContent = message;
            errorMessage.classList.add('show');
            
            setTimeout(() => {
                errorMessage.classList.remove('show');
            }, 5000);
        }

        // Hiển thị lỗi từ server nếu có
        window.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            const error = urlParams.get('error');
            
            if (error === 'invalid') {
                showError('Tên đăng nhập hoặc mật khẩu không đúng');
            } else if (error === 'unauthorized') {
                showError('Vui lòng đăng nhập để tiếp tục');
            } else if (error === 'server') {
                showError('Lỗi kết nối server hoặc database. Vui lòng thử lại sau.');
            }
        });
    </script>
</body>
</html>