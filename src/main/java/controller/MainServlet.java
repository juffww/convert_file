package controller;

import model.bean.conversion;
import model.bean.user;
import model.bo.conversionBO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/main")
public class MainServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendRedirect("/?error=unauthorized");
			return;
		}
		
		user currentUser = (user) session.getAttribute("user");
		int userId = currentUser.getId();
		
		try {
			conversionBO conversionBO = new conversionBO();
			List<conversion> conversions = conversionBO.getUserHistory(userId);
			
			request.setAttribute("conversions", conversions);
			request.setAttribute("username", currentUser.getUsername());
			request.setAttribute("userId", userId);
			
			request.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(request, response);
			
		} catch (Exception e) {
			System.err.println("=== MAIN ERROR ===");
			System.err.println("User ID: " + userId);
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.err.println("======================");
			
			response.sendRedirect("/?error=server");
		}
	}
}
