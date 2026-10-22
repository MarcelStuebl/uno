package htl.steyr.uno.server.database;

import htl.steyr.uno.MatchPlayer;
import htl.steyr.uno.MatchResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DatabaseMatchStates {

    /** Column prefixes of the placements in match_results (index 0 = first place). */
    private static final String[] PLACE_COLUMNS = {"first_place", "second_place", "third_place", "fourth_place"};

    /**
     * Adds a new match together with its result to the database.
     * @param playerIds The user IDs ordered by placement (index 0 = first place).
     * @throws SQLException if a database access error occurs.
     * @throws IllegalArgumentException if there are not 2 to 4 players or a player appears more than once.
     */
    public static void addMatchState(List<Integer> playerIds)  {
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
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add match state.", e);
        }
    }


    /**
     * Inserts a new match and returns its generated ID.
     * @param conn The connection of the running transaction.
     * @return The ID of the new match.
     * @throws SQLException if a database access error occurs.
     */
    private static Integer insertMatch(Connection conn) {
        String query = "INSERT INTO matches () VALUES ()";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert match.", e);
        }
        return null;
    }


    /**
     * Inserts the placements of a match.
     * @param conn The connection of the running transaction.
     * @param matchId The ID of the match.
     * @param playerIds The user IDs ordered by placement (index 0 = first place).
     * @throws SQLException if a database access error occurs.
     */
    private static void insertMatchResult(Connection conn, int matchId, List<Integer> playerIds) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert match result.", e);
        }
    }


    /**
     * Retrieves all matches a user took part in, newest first.
     * Every match contains all of its players (with username) ordered by placement.
     * @param userId The ID of the user.
     * @return The matches of the user.
     */
    public static ArrayList<MatchResult> getMatchResultByUserId(int userId) {
        String query = "SELECT mr.match_id, mr.player_count, m.match_date, " +
                "mr.first_place_user_id, u1.username AS first_place_username, " +
                "mr.second_place_user_id, u2.username AS second_place_username, " +
                "mr.third_place_user_id, u3.username AS third_place_username, " +
                "mr.fourth_place_user_id, u4.username AS fourth_place_username " +
                "FROM match_results mr " +
                "JOIN matches m ON mr.match_id = m.id " +
                "LEFT JOIN user u1 ON u1.id = mr.first_place_user_id " +
                "LEFT JOIN user u2 ON u2.id = mr.second_place_user_id " +
                "LEFT JOIN user u3 ON u3.id = mr.third_place_user_id " +
                "LEFT JOIN user u4 ON u4.id = mr.fourth_place_user_id " +
                "WHERE ? IN (mr.first_place_user_id, mr.second_place_user_id, mr.third_place_user_id, mr.fourth_place_user_id) " +
                "ORDER BY m.match_date DESC, mr.match_id DESC";

        ArrayList<MatchResult> matchResults = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int matchId = rs.getInt("match_id");
                    int playerCount = Math.min(rs.getInt("player_count"), PLACE_COLUMNS.length);
                    ArrayList<MatchPlayer> players = new ArrayList<>();
                    for (int i = 0; i < playerCount; i++) {
                        players.add(new MatchPlayer(
                                rs.getInt(PLACE_COLUMNS[i] + "_user_id"),
                                rs.getString(PLACE_COLUMNS[i] + "_username"),
                                i + 1
                        ));
                    }
                    Timestamp timestamp = rs.getTimestamp("match_date");
                    matchResults.add(new MatchResult(matchId, players, timestamp));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve match result for user ID: " + userId, e);
        }
        return matchResults;
    }
}


