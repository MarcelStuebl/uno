package htl.steyr.uno.server.database;

import htl.steyr.uno.server.PasswordUtil;
import htl.steyr.uno.User;
import htl.steyr.uno.server.exceptions.user.InvalidPasswordException;
import htl.steyr.uno.server.exceptions.user.UserAlreadyExistsException;
import htl.steyr.uno.server.exceptions.user.UserNotFoundException;

import java.sql.*;

public class DatabaseUser {

    /**
     * Retrieves a user by their username from the database.
     * @param username The username of the user to retrieve.
     * @return A User object representing the user.
     * @throws SQLException if a database access error occurs.
     * @throws UserNotFoundException if no user with the given username is found.
     */
    private User getUser(String username) throws SQLException {
        String query = "SELECT id, username, last_name, first_name, games_won, games_lost, created_at, last_login, password_hash, password_salt FROM user WHERE username = ?";

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
                            rs.getInt("games_won"),
                            rs.getInt("games_lost"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_login"),
                            rs.getString("password_hash"),
                            rs.getString("password_salt")
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
     * @throws InvalidPasswordException if the provided password is incorrect.
     */
    public User getUser(String username, String password) throws SQLException {
        User user = getUser(username);
            if (verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt())) {
                updateLastLogin(user.getId());
                return new User(user.getId(), user.getUsername(), user.getLastName(), user.getFirstName(), user.getGamesWon(), user.getGamesLost(), user.getCreatedAt(), user.getLastLogin());
            } else {
                return null;
            }
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
     */
    public void addUser(User user) throws SQLException {
        if (userExists(user)) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        String query = "INSERT INTO user (username, last_name, first_name, created_at, last_login, password_hash, password_salt) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getFirstName());
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(6, user.getPasswordHash());
            pstmt.setString(7, user.getPasswordSalt());
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


}




