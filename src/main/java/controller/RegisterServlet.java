package controller;

import model.bean.user;
import model.dao.userDAO;
import utils.DbConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate input
        if (username == null || password == null || confirmPassword == null ||
            username.trim().isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            response.sendRedirect("register.jsp?error=invalid");
            return;
        }

        username = username.trim();

        // Validate username length
        if (username.length() < 3 || username.length() > 50) {
            response.sendRedirect("register.jsp?error=invalid");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            response.sendRedirect("register.jsp?error=invalid");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            response.sendRedirect("register.jsp?error=invalid");
            return;
        }

        Connection conn = null;
        try {
            conn = DbConnection.getConnection();
            userDAO userDao = new userDAO(conn);

            // Check if username already exists
            if (userDao.isUsernameExists(username)) {
                response.sendRedirect("register.jsp?error=exists");
                return;
            }

            // Create new user
            user newUser = new user();
            newUser.setUsername(username);
            newUser.setPassword(password);

            boolean success = userDao.insert(newUser);

            if (success) {
                response.sendRedirect("register.jsp?success=registered");
            } else {
                response.sendRedirect("register.jsp?error=server");
            }

        } catch (Exception e) {
            System.err.println("=== REGISTER ERROR ===");
            System.err.println("Username: " + username);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("======================");

            response.sendRedirect("register.jsp?error=server");
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect to register page
        response.sendRedirect("register.jsp");
    }
}
