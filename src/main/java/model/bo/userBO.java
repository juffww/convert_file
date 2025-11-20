package model.bo;

import model.bean.user;
import model.dao.userDAO;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Pattern;

public class userBO {
    private userDAO userDAO;

    public userBO(Connection conn) {
        this.userDAO = new userDAO(conn);
    }

    // Đăng ký user mới
    public boolean register(user user) {
        // Validate input
        String validation = validateUser(user);
        if (validation != null) {
            System.err.println("Validation error: " + validation);
            return false;
        }

        if (userDAO.getByUsername(user.getUsername()) != null) {
            System.err.println("Username đã tồn tại");
            return false;
        }

        return userDAO.insert(user);
    }

    public user login(String username, String password) {
        return userDAO.login(username, password);
    }

    // Lấy thông tin user
    public user getUser(int userId) {
        return userDAO.getById(userId);
    }

    // Lấy user theo username
    public user getUserByUsername(String username) {
        return userDAO.getByUsername(username);
    }

    // Lấy tất cả users (admin)
    public List<user> getAllusers() {
        return userDAO.getAll();
    }

    // Validate user input
    private String validateUser(user user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "Username không được để trống";
        }

        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            return "Username phải từ 3-50 ký tự";
        }

        if (!Pattern.matches("^[a-zA-Z0-9_]+$", user.getUsername())) {
            return "Username chỉ được chứa chữ, số và dấu gạch dưới";
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Password không được để trống";
        }

        if (!validatePassword(user.getPassword())) {
            return "Password phải có ít nhất 6 ký tự";
        }

        return null;
    }

    // Validate password
    private boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Kiểm tra user có tồn tại không
    public boolean isUserExists(String username) {
        return userDAO.getByUsername(username) != null;
    }
}
