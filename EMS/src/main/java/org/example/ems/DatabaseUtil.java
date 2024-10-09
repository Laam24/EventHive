package org.example.ems;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/eventhive";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final int TOKEN_EXPIRY_HOURS = 24;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertContactMessage(String name, String email, String message) {
        String query = "INSERT INTO contact_messages (name, email, message) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, message);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<ContactMessage> getContactMessages() {
        List<ContactMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM contact_messages ORDER BY submission_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                ContactMessage message = new ContactMessage(
                        rs.getInt("id"),
                        rs.getString("name"),  // Corrected this line
                        rs.getString("email"),
                        rs.getString("message"),
                        rs.getTimestamp("submission_date").toLocalDateTime()
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public static boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    public static boolean storeResetToken(String email, String resetToken) {
        String query = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, resetToken);
            pstmt.setTimestamp(2, Timestamp.from(Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS)));
            pstmt.setString(3, email);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String verifyTokenAndGetEmail(String token) {
        String query = "SELECT email FROM users WHERE reset_token = ? AND reset_token_expiry > ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, token);
            pstmt.setTimestamp(2, Timestamp.from(Instant.now()));
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

        String query = "UPDATE users SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            // In a real application, you should hash the password before storing it
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean verifyResetToken(String resetToken) {
        String query = "SELECT * FROM users WHERE reset_token = ? AND reset_token_expiry > NOW()";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, resetToken);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updatePassword(String resetToken, String newPassword) {
        String query = "UPDATE users SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE reset_token = ? AND reset_token_expiry > NOW()";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newPassword); // In a real app, hash the password
            pstmt.setString(2, resetToken);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean registerUser(String firstName, String lastName, String username, String email, String contact, String password) throws SQLException {
        String query = "INSERT INTO users (first_name, last_name, username, email, contact, password) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, username);
            pstmt.setString(4, email);
            pstmt.setString(5, contact);
            pstmt.setString(6, password); // Note: In a real application, you should hash the password before storing it
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static List<Host> getHosts() {
        List<Host> hosts = new ArrayList<>();
        String query = "SELECT * FROM hosts";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Host host = new Host(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialty"),
                        rs.getDouble("rating"),
                        rs.getString("image_url")
                );
                hosts.add(host);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hosts;
    }

    public static List<Host> getHostsSortedByRating() {
        List<Host> hosts = getHosts();
        hosts.sort((h1, h2) -> Double.compare(h2.getRating(), h1.getRating()));
        return hosts;
    }

    public static List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM events";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Event event = new Event(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("event_date").toLocalDate(),
                        rs.getTime("event_time").toLocalTime(),
                        rs.getInt("host_id"),
                        rs.getString("category"),
                        rs.getString("banner_path"),
                        rs.getString("event_type")
                );
                event.setId(rs.getInt("id"));
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public static boolean insertEvent(Event event) {
        String query = "INSERT INTO events (name, description, event_date, event_time, host_id, category, banner_path, event_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, event.getName());
            pstmt.setString(2, event.getDescription());
            pstmt.setDate(3, Date.valueOf(event.getEventDate()));
            pstmt.setTime(4, Time.valueOf(event.getEventTime()));
            pstmt.setInt(5, event.getHostId());
            pstmt.setString(6, event.getCategory());
            pstmt.setString(7, event.getBannerPath());
            pstmt.setString(8, event.getEventType());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getCurrentUser() {
        String currentUsername = LoggedInUser.getUsername();
        System.out.println("Getting current user for username: " + currentUsername);
        if (currentUsername == null) {
            System.out.println("No logged in user found");
            return null;
        }
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currentUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("contact"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validatePassword(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(User user) {
        String query = "UPDATE users SET first_name = ?, last_name = ?, email = ?, contact = ?, password = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getContact());
            pstmt.setString(5, user.getPassword());
            pstmt.setInt(6, user.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}