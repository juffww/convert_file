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
        int generatedId = -1;

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, conv.getUserId());
            ps.setString(2, conv.getInputUrl());
            ps.setString(3, conv.getInputPublicId());
            ps.setString(4, conv.getInputFilename());
            ps.setString(5, "PENDING");

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

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                conversion c = new conversion();
                c.setId(rs.getInt("id"));
                c.setUserId(rs.getInt("user_id"));
                c.setInputUrl(rs.getString("input_url"));
                c.setInputPublicId(rs.getString("input_public_id"));
                c.setInputFilename(rs.getString("input_filename"));
                c.setOutputUrl(rs.getString("output_url"));
                c.setOutputPublicId(rs.getString("output_public_id"));
                c.setStatus(rs.getString("status"));
                c.setErrorMessage(rs.getString("error_message"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setUpdatedAt(rs.getTimestamp("updated_at"));
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
        try (Connection conn = DbConnection.getConnection();
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
    public void updateConversionResult(int conversionId, String outputUrl, String outputPublicId) {
        String sql = "UPDATE conversions SET status = 'COMPLETED', output_url = ?, output_public_id = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, outputUrl);
            ps.setString(2, outputPublicId);
            ps.setInt(3, conversionId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}