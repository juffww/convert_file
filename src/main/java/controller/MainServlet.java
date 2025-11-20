package controller;

// import model.bean.file;
// import model.dao.fileDAO;
import model.bean.user;
import utils.DbConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/main")
public class MainServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		// Kiểm tra session
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendRedirect("index.html?error=unauthorized");
			return;
		}
		
		user currentUser = (user) session.getAttribute("user");
		// int userId = currentUser.getUserId();
		
		Connection conn = null;
		try {
			conn = DbConnection.getConnection();
			// fileDAO fileDao = new fileDAO(conn);
			
			// Lấy danh sách file của user
			// List<file> userFiles = fileDao.getByUserId(userId);
			
			// Set attributes để JSP sử dụng
			// request.setAttribute("files", userFiles);
			// request.setAttribute("username", currentUser.getUsername());
			// request.setAttribute("userId", userId);
			
			// Forward to JSP
			request.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(request, response);
			
		} catch (Exception e) {
			System.err.println("=== DASHBOARD ERROR ===");
			// System.err.println("User ID: " + userId);
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.err.println("======================");
			
			response.sendRedirect("index.html?error=server");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
