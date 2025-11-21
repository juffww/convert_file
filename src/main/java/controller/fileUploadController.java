package controller;

import model.bean.conversion;
import model.bean.user;
import model.bo.conversionBO;
import utils.imageKitConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@WebServlet("/upload")
@MultipartConfig(
	fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
	maxFileSize = 1024 * 1024 * 50,       // 50MB
	maxRequestSize = 1024 * 1024 * 100    // 100MB
)
public class fileUploadController extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		// Kiểm tra session
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendRedirect("/?error=unauthorized");
			return;
		}
		
		user currentUser = (user) session.getAttribute("user");
		int userId = currentUser.getId();
		
		try {
			// Lấy file từ request
			Part filePart = request.getPart("pdfFile");
			if (filePart == null || filePart.getSize() == 0) {
				response.sendRedirect("main?error=nofile");
				return;
			}
			
			String fileName = getFileName(filePart);
			
			// Kiểm tra file PDF
			if (!fileName.toLowerCase().endsWith(".pdf")) {
				response.sendRedirect("main?error=invalidtype");
				return;
			}
			
			// Upload file lên ImageKit
			ImageKit imageKit = imageKitConnection.getInstance();
			
			// Đọc file thành byte array và convert sang Base64
			byte[] fileBytes;
			try (InputStream inputStream = filePart.getInputStream()) {
				fileBytes = inputStream.readAllBytes();
			}
			String base64File = Base64.getEncoder().encodeToString(fileBytes);
			
			// Tạo tên file unique
			String uniqueFileName = System.currentTimeMillis() + "_" + userId + "_" + fileName;
			
			// Upload lên ImageKit
			FileCreateRequest fileCreateRequest = new FileCreateRequest(
				base64File,
				uniqueFileName
			);
			fileCreateRequest.setFolder("/pdf_uploads/");
			fileCreateRequest.setUseUniqueFileName(false);
			
			Result uploadResult = imageKit.upload(fileCreateRequest);
			
			if (uploadResult == null) {
				response.sendRedirect("main?error=upload");
				return;
			}
			
			// Lấy thông tin file đã upload
			String fileUrl = uploadResult.getUrl();
			String fileId = uploadResult.getFileId();
			
			// Tạo object conversion
			conversion conv = new conversion();
			conv.setUserId(userId);
			conv.setInputUrl(fileUrl);
			conv.setInputPublicId(fileId);
			conv.setInputFilename(fileName);
			conv.setStatus("UPLOADED");
			
			// Lưu vào database thông qua BO
			conversionBO conversionBO = new conversionBO();
			boolean success = conversionBO.uploadAndQueue(conv);
			
			if (success) {
				response.sendRedirect("main?success=uploaded");
			} else {
				response.sendRedirect("main?error=dbfailed");
			}
			
		} catch (Exception e) {
			System.err.println("=== UPLOAD ERROR ===");
			System.err.println("User ID: " + userId);
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.err.println("====================");
			
			response.sendRedirect("main?error=upload");
		}
	}
	
	private String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] tokens = contentDisp.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf("=") + 2, token.length() - 1);
			}
		}
		return "";
	}
}
