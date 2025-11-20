package model.dao;

import model.bean.conversion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.DbConnection;

public class conversionDAO {

    // 1. Tạo mới 1 bản ghi Conversion (Khi user vừa upload xong)
    public int createConversion(conversion conv) {
        String sql = "INSERT INTO conversions (user_id, input_url, input_public_id, input_filename, status) VALUES (?, ?, ?, ?, ?)";
        // Return ID vừa sinh ra để gửi vào RabbitMQ
        int generatedId = -1;

        try (Connection conn = new DbConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, conv.getUserId());
            ps.setString(2, conv.getInputUrl());
            ps.setString(3, conv.getInputPublicId());
            ps.setString(4, conv.getInputFilename());
            ps.setString(5, "PENDING"); // Set luôn pending để chờ queue

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    // 2. Lấy danh sách lịch sử convert của User
    public List<conversion> getHistoryByUserId(int userId) {
        List<conversion> list = new ArrayList<>();
        String sql = "SELECT * FROM conversions WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = new DbConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversion c = new conversion();
                c.setId(rs.getInt("id"));
                c.setUserId(rs.getInt("user_id"));
                c.setInputFilename(rs.getString("input_filename"));
                c.setStatus(rs.getString("status"));
                c.setOutputUrl(rs.getString("output_url"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                // ... set thêm các trường khác nếu cần hiển thị
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. Update trạng thái (Dùng cho Worker: PENDING -> PROCESSING -> COMPLETED)
    public void updateStatus(int conversionId, String status, String errorMessage) {
        String sql = "UPDATE conversions SET status = ?, error_message = ? WHERE id = ?";
        try (Connection conn = new DbConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, errorMessage);
            ps.setInt(3, conversionId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4. Update kết quả thành công (Dùng cho Worker khi xong việc)
    public void updateconversionResult(int conversionId, String outputUrl, String outputPublicId) {
        String sql = "UPDATE conversions SET status = 'COMPLETED', output_url = ?, output_public_id = ? WHERE id = ?";
        try (Connection conn = new DbConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, outputUrl);
            ps.setString(2, outputPublicId);
            ps.setInt(3, conversionId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 5. Lấy chi tiết 1 conversion (Worker cần cái này để biết input_url ở đâu mà tải về)
    public conversion getconversionById(int id) {
        String sql = "SELECT * FROM conversions WHERE id = ?";
        try (Connection conn = new DbConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                conversion c = new conversion();
                c.setId(rs.getInt("id"));
                c.setInputUrl(rs.getString("input_url"));
                c.setInputPublicId(rs.getString("input_public_id"));
                c.setUserId(rs.getInt("user_id"));
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}