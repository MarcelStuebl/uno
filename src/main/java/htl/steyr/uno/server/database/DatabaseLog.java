package htl.steyr.uno.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DatabaseLog {


    public void logUserLogin(Integer userId, String username, String ipAddress, boolean correctPassword) {
        String query = "INSERT INTO log_login (user_id, username, ip_address, correct_password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (userId != null) {
                pstmt.setInt(1, userId);
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, username);
            pstmt.setString(3, ipAddress);
            pstmt.setBoolean(4, correctPassword);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
