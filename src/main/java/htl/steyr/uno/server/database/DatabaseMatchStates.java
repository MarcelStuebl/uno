package htl.steyr.uno.server.database;

import htl.steyr.uno.MatchResult;

import java.sql.*;
import java.util.HashSet;
import java.util.List;

public class DatabaseMatchStates {

    /**
     * Adds a new match together with its result to the database.
     * @param playerIds The user IDs ordered by placement (index 0 = first place).
     * @throws SQLException if a database access error occurs.
     * @throws IllegalArgumentException if there are not 2 to 4 players or a player appears more than once.
     */
    public void addMatchState(List<Integer> playerIds) throws SQLException {
        int playerCount = playerIds.size();
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("A match needs 2 to 4 players.");
        }
        if (new HashSet<>(playerIds).size() != playerCount) {
            throw new IllegalArgumentException("A player can only appear once per match.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int matchId = insertMatch(conn);
                insertMatchResult(conn, matchId, playerIds);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }


    /**
     * Inserts a new match and returns its generated ID.
     * @param conn The connection of the running transaction.
     * @return The ID of the new match.
     * @throws SQLException if a database access error occurs.
     */
    private int insertMatch(Connection conn) throws SQLException {
        String query = "INSERT INTO matches () VALUES ()";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create match: no ID returned.");
    }


    /**
     * Inserts the placements of a match.
     * @param conn The connection of the running transaction.
     * @param matchId The ID of the match.
     * @param playerIds The user IDs ordered by placement (index 0 = first place).
     * @throws SQLException if a database access error occurs.
     */
    private void insertMatchResult(Connection conn, int matchId, List<Integer> playerIds) throws SQLException {
        String query = "INSERT INTO match_results (match_id, player_count, first_place_user_id, second_place_user_id, third_place_user_id, fourth_place_user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, matchId);
            pstmt.setInt(2, playerIds.size());

            for (int i = 0; i < 4; i++) {
                if (i < playerIds.size()) {
                    pstmt.setInt(3 + i, playerIds.get(i));
                } else {
                    pstmt.setNull(3 + i, Types.INTEGER);
                }
            }
            pstmt.executeUpdate();
        }
    }


    public MatchResult getMatchResultByUserId(int userId) throws SQLException {
        String query = "SELECT mr.match_id, mr.player_count, mr.first_place_user_id, mr.second_place_user_id, mr.third_place_user_id, mr.fourth_place_user_id, m.match_date " +
                "FROM match_results mr " +
                "JOIN matches m ON mr.match_id = m.id " +
                "WHERE ? IN (mr.first_place_user_id, mr.second_place_user_id, mr.third_place_user_id, mr.fourth_place_user_id)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int matchId = rs.getInt("match_id");
                    int playerCount = rs.getInt("player_count");
                    int[] playerIds = new int[playerCount];
                    for (int i = 0; i < playerCount; i++) {
                        playerIds[i] = rs.getInt(3 + i); // 3 + i corresponds to the column index of the player IDs
                    }
                    Timestamp timestamp = rs.getTimestamp("match_date");
                    return new MatchResult(matchId, playerIds, timestamp);
                }
            }
        }
        return null; // No match found for the given user ID
    }
}


