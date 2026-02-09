package htl.steyr.uno.server.database;

import htl.steyr.uno.User;

import java.sql.*;

public class DatabaseUser {

    /**
     * Retrieves a user by their username from the database.
     * @param username The username of the user to retrieve.
     * @return A User object representing the user, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public User getUser(String username) throws SQLException {
        String query = "SELECT id, username, lastname, firstname FROM user WHERE username = ?";

        User user = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("lastname"),
                            rs.getString("firstname")
                    );
                }
            }
        }
        return user;
    }


    /**
     * Adds a new user to the database.
     * @param user The User object to be added.
     * @throws SQLException if a database access error occurs.
     */
    public void addUser(User user) throws SQLException {
        String query = "INSERT INTO user (username, lastname, firstname) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getFirstName());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }


    /**
     * Checks if a user with the given username already exists in the database.
     * @param username The username to check for existence.
     * @return true if the user exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean userExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }




}




