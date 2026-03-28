package htl.steyr.uno.server.database;

import htl.steyr.uno.server.PasswordUtil;
import htl.steyr.uno.User;
import htl.steyr.uno.server.exceptions.database.UserAlreadyExistsException;

import java.sql.*;

public class DatabaseUser {

    /**
     * Retrieves a user by their username from the database.
     * @param username The username of the user to retrieve.
     * @return A User object representing the user.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserPerUserName(String username) throws SQLException {
        String query = "SELECT id, username, last_name, first_name, email, games_won, games_lost, created_at, last_login, password_hash, password_salt, profile_image FROM user WHERE username = ?";

        User user = new User();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getString("email"),
                            rs.getInt("games_won"),
                            rs.getInt("games_lost"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_login"),
                            rs.getString("password_hash"),
                            rs.getString("password_salt"),
                            rs.getBytes("profile_image")
                    );
                }
            }
        }
        return user;
    }


    /**
     * Retrieves a user by their username and verifies the provided password.
     * @param username The username of the user to retrieve.
     * @param password The password to verify against the stored hash and salt.
     * @return A User object representing the user if the password is correct.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserPerUserName(String username, String password) throws SQLException {
        User user = getUserPerUserName(username);
            if (verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt())) {
                updateLastLogin(user.getId());
                return new User(user.getId(), user.getUsername(), user.getLastName(), user.getFirstName(), user.getEmail(), user.getGamesWon(), user.getGamesLost(), user.getCreatedAt(), user.getLastLogin(), user.getProfileImageData());
            } else {
                return null;
            }
    }


    /**
     * Retrieves a user by their email from the database.
     * @param email The email of the user to retrieve.
     * @return A User object representing the user.
     * @throws SQLException if a database access error occurs.
     */
    public User getUserPerEmail(String email) throws SQLException {
        String query = "SELECT id, username, last_name, first_name, email, games_won, games_lost, created_at, last_login, profile_image FROM user WHERE email = ?";

        User user = new User();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getString("email"),
                            rs.getInt("games_won"),
                            rs.getInt("games_lost"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_login"),
                            rs.getBytes("profile_image")
                    );
                }
            }
        }
        return user;
    }


    /**
     * Updates the password for a user in the database.
     * @param username The username of the user whose password is to be updated.
     * @param newPassword The new password to be set for the user.
     * @return A User object representing the updated user.
     * @throws SQLException if a database access error occurs.
     */
    public User updatePassword(String username, String newPassword) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(newPassword, salt);

        String query = "UPDATE user SET password_hash = ?, password_salt = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hash);
            pstmt.setString(2, salt);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        }

        return getUserPerUserName(username, newPassword);
    }


    /**
     * Verifies a password against the stored hash and salt.
     * @param password The password to verify.
     * @param storedHash The stored password hash.
     * @param storedSalt The stored password salt.
     * @return true if the password is correct, false otherwise.
     */
    private boolean verifyPassword(String password, String storedHash, String storedSalt) {
        String hashToVerify = PasswordUtil.hashPassword(password, storedSalt);
        return hashToVerify.equals(storedHash);
    }


    /**
     * Adds a new user to the database.
     * @param user The User object to be added.
     * @throws SQLException if a database access error occurs.
     * @throws UserAlreadyExistsException if a user with the same username already exists in the database.
     */
    public void addUser(User user) throws SQLException, UserAlreadyExistsException {
        if (userExists(user)) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        String query = "INSERT INTO user (username, last_name, first_name, email, created_at, last_login, password_hash, password_salt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getEmail());
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(7, user.getPasswordHash());
            pstmt.setString(8, user.getPasswordSalt());
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
     * @param user The username to check for existence.
     * @return true if the user exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean userExists(User user) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }


    /**
     * Updates the last login timestamp for a user in the database.
     * @param userId The ID of the user to update.
     * @throws SQLException if a database access error occurs.
     */
    private void updateLastLogin(int userId) throws SQLException {
        String query = "UPDATE user SET last_login = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }


    public void updateProfileImage(User user, byte[] imageData) throws SQLException {
        String query = "UPDATE user SET profile_image = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBytes(1, imageData);
            pstmt.setString(2, user.getUsername());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new SQLException("Failed to update profile image: " + e.getMessage());
        }
    }


}




