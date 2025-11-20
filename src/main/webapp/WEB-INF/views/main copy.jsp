<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.bean.file" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>main - PDF to DOCX Converter</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        .header {
            background: white;
            padding: 20px 30px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            color: #667eea;
            font-size: 24px;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .user-name {
            font-weight: 500;
            color: #333;
            padding: 8px 16px;
            background: #f0f0f0;
            border-radius: 20px;
        }

        .logout-btn {
            padding: 8px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s ease;
        }

        .logout-btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }

        .upload-section {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
        }

        .upload-section h2 {
            color: #333;
            margin-bottom: 20px;
            font-size: 20px;
        }

        .upload-form {
            display: flex;
            gap: 15px;
            align-items: flex-end;
            flex-wrap: wrap;
        }

        .file-input-wrapper {
            flex: 1;
            min-width: 250px;
        }

        .file-input-wrapper label {
            display: block;
            margin-bottom: 8px;
            color: #666;
            font-size: 14px;
            font-weight: 500;
        }

        .file-input {
            width: 100%;
            padding: 10px;
            border: 2px dashed #667eea;
            border-radius: 8px;
            background: #f8f9ff;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .file-input:hover {
            border-color: #5568d3;
            background: #f0f2ff;
        }

        .upload-btn {
            padding: 12px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }

        .upload-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
        }

        .upload-btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
            transform: none;
        }

        .files-section {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
        }

        .files-section h2 {
            color: #333;
            margin-bottom: 20px;
            font-size: 20px;
        }

        .files-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        .files-table thead {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .files-table th {
            padding: 15px;
            text-align: left;
            font-weight: 600;
            font-size: 14px;
        }

        .files-table td {
            padding: 15px;
            border-bottom: 1px solid #e0e0e0;
        }

        .files-table tbody tr {
            transition: all 0.2s ease;
        }

        .files-table tbody tr:hover {
            background: #f8f9ff;
        }

        .file-name {
            color: #333;
            font-weight: 500;
        }

        .file-size {
            color: #666;
            font-size: 14px;
        }

        .file-date {
            color: #999;
            font-size: 13px;
        }

        .action-btn {
            padding: 6px 15px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 13px;
            transition: all 0.2s ease;
            margin-right: 5px;
        }

        .convert-btn {
            background: #667eea;
            color: white;
        }

        .convert-btn:hover {
            background: #5568d3;
        }

        .download-btn {
            background: #4CAF50;
            color: white;
        }

        .download-btn:hover {
            background: #45a049;
        }

        .delete-btn {
            background: #f44336;
            color: white;
        }

        .delete-btn:hover {
            background: #da190b;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #999;
        }

        .empty-state-icon {
            font-size: 64px;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .empty-state-text {
            font-size: 18px;
            margin-bottom: 10px;
        }

        .empty-state-subtext {
            font-size: 14px;
        }

        .message {
            padding: 12px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: none;
        }

        .message.show {
            display: block;
        }

        .message.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .message.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .file-info-badge {
            display: inline-block;
            padding: 4px 12px;
            background: #e3f2fd;
            color: #1976d2;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
        }

        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 15px;
                text-align: center;
            }

            .upload-form {
                flex-direction: column;
            }

            .files-table {
                font-size: 13px;
            }

            .files-table th,
            .files-table td {
                padding: 10px 8px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>📄 PDF to DOCX Converter</h1>
            <div class="user-info">
                <span class="user-name">👤 <%= request.getAttribute("username") %></span>
                <form action="logout" method="POST" style="display:inline;">
                    <button type="submit" class="logout-btn">Đăng xuất</button>
                </form>
            </div>
        </div>

        <!-- Messages -->
        <div id="messageArea"></div>

        <!-- Upload Section -->
        <div class="upload-section">
            <h2>📤 Tải lên file PDF</h2>
            <form action="upload" method="POST" enctype="multipart/form-data" class="upload-form" id="uploadForm">
                <div class="file-input-wrapper">
                    <label for="pdfFile">Chọn file PDF:</label>
                    <input 
                        type="file" 
                        id="pdfFile" 
                        name="pdfFile" 
                        accept=".pdf,application/pdf" 
                        required 
                        class="file-input"
                        onchange="updateFileName()"
                    >
                    <small id="fileName" style="color: #666; margin-top: 5px; display: block;"></small>
                </div>
                <button type="submit" class="upload-btn" id="uploadBtn">
                    <span id="uploadBtnText">Tải lên</span>
                </button>
            </form>
        </div>

        <!-- Files List Section -->
        <div class="files-section">
            <!-- <h2>📂 Danh sách file của bạn</h2>
            
            <%
                List<file> files = (List<file>) request.getAttribute("files");
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                DecimalFormat sizeFormat = new DecimalFormat("#,##0.00");
                
                if (files != null && !files.isEmpty()) {
            %>
            <div class="file-info-badge">
                Tổng số file: <%= files.size() %>
            </div> -->
            
            <table class="files-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên file</th>
                        <th>Kích thước</th>
                        <th>Ngày tải lên</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <!-- <tbody>
                    <%
                        for (file f : files) {
                            double fileSizeKB = f.getFileSize() / 1024.0;
                            double fileSizeMB = fileSizeKB / 1024.0;
                            String sizeDisplay = fileSizeMB >= 1 
                                ? sizeFormat.format(fileSizeMB) + " MB" 
                                : sizeFormat.format(fileSizeKB) + " KB";
                    %>
                    <tr>
                        <td><strong>#<%= f.getFileId() %></strong></td>
                        <td class="file-name">📄 <%= f.getOriginalFilename() %></td>
                        <td class="file-size"><%= sizeDisplay %></td>
                        <td class="file-date"><%= dateFormat.format(f.getUploadTime()) %></td>
                        <td>
                            <button class="action-btn convert-btn" onclick="convertFile(<%= f.getFileId() %>)">
                                🔄 Chuyển đổi
                            </button>
                            <button class="action-btn download-btn" onclick="downloadFile(<%= f.getFileId() %>)">
                                ⬇️ Tải về
                            </button>
                            <button class="action-btn delete-btn" onclick="deleteFile(<%= f.getFileId() %>, '<%= f.getOriginalFilename().replace("'", "\\'") %>')">
                                🗑️ Xóa
                            </button>
                        </td>
                    </tr>
                    <% } %>
                </tbody> -->
            </table>
            <% } else { %>
            <div class="empty-state">
                <div class="empty-state-icon">📭</div>
                <div class="empty-state-text">Chưa có file nào</div>
                <div class="empty-state-subtext">Hãy tải lên file PDF đầu tiên của bạn!</div>
            </div>
            <% } %>
        </div>
    </div>

    <script>
        // Hiển thị message từ URL params
        window.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            const success = urlParams.get('success');
            const error = urlParams.get('error');
            
            if (success === 'uploaded') {
                showMessage('✅ File đã được tải lên thành công!', 'success');
            } else if (error === 'nofile') {
                showMessage('❌ Vui lòng chọn file để tải lên!', 'error');
            } else if (error === 'invalidtype') {
                showMessage('❌ Chỉ chấp nhận file PDF!', 'error');
            } else if (error === 'upload') {
                showMessage('❌ Lỗi khi tải file lên. Vui lòng thử lại!', 'error');
            } else if (error === 'dbfailed') {
                showMessage('❌ Lỗi lưu thông tin file vào database!', 'error');
            }
        });

        function showMessage(text, type) {
            const messageArea = document.getElementById('messageArea');
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message ' + type + ' show';
            messageDiv.textContent = text;
            messageArea.appendChild(messageDiv);
            
            setTimeout(() => {
                messageDiv.classList.remove('show');
                setTimeout(() => messageDiv.remove(), 300);
            }, 5000);
        }

        function updateFileName() {
            const fileInput = document.getElementById('pdfFile');
            const fileName = document.getElementById('fileName');
            if (fileInput.files.length > 0) {
                const file = fileInput.files[0];
                const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
                fileName.textContent = `Đã chọn: ${file.name} (${sizeMB} MB)`;
            } else {
                fileName.textContent = '';
            }
        }

        // Form validation
        document.getElementById('uploadForm').addEventListener('submit', function(e) {
            const fileInput = document.getElementById('pdfFile');
            const uploadBtn = document.getElementById('uploadBtn');
            const uploadBtnText = document.getElementById('uploadBtnText');
            
            if (fileInput.files.length === 0) {
                e.preventDefault();
                showMessage('❌ Vui lòng chọn file!', 'error');
                return false;
            }
            
            const file = fileInput.files[0];
            if (!file.name.toLowerCase().endsWith('.pdf')) {
                e.preventDefault();
                showMessage('❌ Chỉ chấp nhận file PDF!', 'error');
                return false;
            }
            
            // Disable button và hiển thị loading
            uploadBtn.disabled = true;
            uploadBtnText.textContent = 'Đang tải lên...';
        });

        function convertFile(fileId) {
            if (confirm('Bạn có muốn chuyển đổi file này sang DOCX?')) {
                window.location.href = 'convert?fileId=' + fileId;
            }
        }

        function downloadFile(fileId) {
            window.location.href = 'download?fileId=' + fileId;
        }

        function deleteFile(fileId, fileName) {
            if (confirm('Bạn có chắc muốn xóa file "' + fileName + '"?')) {
                window.location.href = 'delete?fileId=' + fileId;
            }
        }
    </script>
</body>
</html>
