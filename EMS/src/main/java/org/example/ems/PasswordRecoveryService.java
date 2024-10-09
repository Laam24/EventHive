package org.example.ems;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordRecoveryService {
    private static final int TOKEN_EXPIRY_HOURS = 24;

    public static String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    public static void sendResetEmail(String email, String resetToken) {
        // Simulating email sending
        System.out.println("Sending reset email to: " + email);
        System.out.println("Reset token: " + resetToken);
    }

    public static boolean storeResetToken(String email, String token) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO password_reset_tokens (user_id, token, created_at) VALUES ((SELECT id FROM users WHERE email = ?), ?, ?)")) {
            pstmt.setString(1, email);
            pstmt.setString(2, token);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String verifyTokenAndGetEmail(String token) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT u.email FROM users u JOIN password_reset_tokens prt ON u.id = prt.user_id WHERE prt.token = ? AND prt.created_at > ?")) {
            pstmt.setString(1, token);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().minusHours(TOKEN_EXPIRY_HOURS)));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean resetPassword(String token, String newPassword) {
        String email = verifyTokenAndGetEmail(token);
        if (email == null) {
            return false;
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?")) {
            // In a real application, you should hash the password before storing it
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                invalidateToken(token);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void invalidateToken(String token) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM password_reset_tokens WHERE token = ?")) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}