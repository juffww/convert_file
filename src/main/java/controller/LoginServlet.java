package controller;

import model.bean.user;
import model.bo.userBO;
import utils.DbConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;

@WebServlet("/auth")
public class LoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String remember = request.getParameter("remember");

		if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
			response.sendRedirect("index.jsp?error=invalid");
			return;
		}

		Connection conn = null;
		try {
			conn = DbConnection.getConnection();
			userBO ubo = new userBO(conn);
			user u = ubo.login(username.trim(), password);

			if (u != null) {
				HttpSession session = request.getSession(true);
				session.setAttribute("user", u);
				session.setAttribute("userId", u.getId());
				session.setAttribute("username", u.getUsername());
				session.setMaxInactiveInterval(3600);

				if (remember != null && (remember.equals("on") || remember.equals("true"))) {
					Cookie c = new Cookie("username", username);
					c.setMaxAge(7 * 24 * 3600); // 7 days
					c.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
					response.addCookie(c);
				} else {
					Cookie c = new Cookie("username", "");
					c.setMaxAge(0);
					c.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
					response.addCookie(c);
				}

				response.sendRedirect("main");
			} else {
				response.sendRedirect("index.jsp?error=invalid");
			}
		} catch (Exception e) {
			System.err.println("=== LOGIN ERROR ===");
			System.err.println("Username: " + username);
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();

			response.sendRedirect("index.jsp?error=server");
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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp");
	}
}
