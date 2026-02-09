package htl.steyr.uno.server.database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String HOST = dotenv.get("DB_HOST");
    private static final String DATABASE = dotenv.get("DB_DATABASE");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static final String URL = "jdbc:mysql://" + HOST + ":3306/" + DATABASE
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Berlin";

    private static Connection connection = null;


    /**
     * Establishes and returns a connection to the MySQL database.
     * @return Connection object to the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver nicht gefunden", e);
            }
        }
        return connection;
    }



}




