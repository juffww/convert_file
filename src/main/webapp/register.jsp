<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - PDF to DOCX Converter</title>
    <link rel="stylesheet" href="style.css">
    <style>
        
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>Đăng ký</h1>
        </div>
        
        <form class="login-form" action="register" method="POST" onsubmit="return validateForm()">
            <div class="error-message" id="errorMessage"></div>
            <div class="success-message" id="successMessage"></div>
            
            <div class="form-group">
                <label for="username">Tên đăng nhập *</label>
                <input 
                    type="text" 
                    id="username" 
                    name="username" 
                    placeholder="Nhập tên đăng nhập"
                    required
                    autocomplete="username"
                    minlength="3"
                    maxlength="50"
                >
                <div class="password-requirements">
                    Tối thiểu 3 ký tự, tối đa 50 ký tự
                </div>
            </div>

            <div class="form-group">
                <label for="password">Mật khẩu *</label>
                <input 
                    type="password" 
                    id="password" 
                    name="password" 
                    placeholder="Nhập mật khẩu"
                    required
                    autocomplete="new-password"
                    minlength="6"
                >
                <div class="password-requirements">
                    Tối thiểu 6 ký tự
                </div>
            </div>

            <div class="form-group">
                <label for="confirmPassword">Xác nhận mật khẩu *</label>
                <input 
                    type="password" 
                    id="confirmPassword" 
                    name="confirmPassword" 
                    placeholder="Nhập lại mật khẩu"
                    required
                    autocomplete="new-password"
                >
            </div>

            <button type="submit" class="login-button">Đăng ký</button>

            <div class="divider">
                <span>hoặc</span>
            </div>

            <div class="login-link">
                Đã có tài khoản? <a href="index.jsp">Đăng nhập </a>
            </div>
        </form>
    </div>

    <script>
        function validateForm() {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            // Reset messages
            document.getElementById('errorMessage').classList.remove('show');
            document.getElementById('successMessage').classList.remove('show');

            // Validate username
            if (username.length < 3) {
                showError('Tên đăng nhập phải có ít nhất 3 ký tự');
                return false;
            }

            if (username.length > 50) {
                showError('Tên đăng nhập không được vượt quá 50 ký tự');
                return false;
            }

            // Validate username characters
            const usernameRegex = /^[a-zA-Z0-9_]+$/;
            if (!usernameRegex.test(username)) {
                showError('Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới');
                return false;
            }

            // Validate password
            if (password.length < 6) {
                showError('Mật khẩu phải có ít nhất 6 ký tự');
                return false;
            }

            // Validate password confirmation
            if (password !== confirmPassword) {
                showError('Mật khẩu xác nhận không khớp');
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

        function showSuccess(message) {
            const successMessage = document.getElementById('successMessage');
            successMessage.textContent = message;
            successMessage.classList.add('show');
        }

        // Hiển thị thông báo từ server nếu có
        window.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            const error = urlParams.get('error');
            const success = urlParams.get('success');
            
            if (error === 'exists') {
                showError('Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.');
            } else if (error === 'invalid') {
                showError('Thông tin đăng ký không hợp lệ');
            } else if (error === 'server') {
                showError('Lỗi server. Vui lòng thử lại sau.');
            } else if (success === 'registered') {
                showSuccess('Đăng ký thành công! Đang chuyển hướng...');
                setTimeout(() => {
                    window.location.href = 'index.jsp';
                }, 2000);
            }
        });
    </script>
</body>
</html>
