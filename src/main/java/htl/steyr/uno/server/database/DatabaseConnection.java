package htl.steyr.uno.server.database;

import htl.steyr.uno.server.exceptions.database.DatabaseException;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value != null) return value;
        return dotenv.get(key);
    }

    private static final String HOST = getEnv("DB_HOST");
    private static final String DATABASE = getEnv("DB_DATABASE");
    private static final String USER = getEnv("DB_USER");
    private static final String PASSWORD = getEnv("DB_PASSWORD");

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
            } catch (Exception e) {
                throw new DatabaseException("JDBC-Treiber nicht gefunden");
            }
        }
        return connection;
    }



}




