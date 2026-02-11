package htl.steyr.uno.server;

import htl.steyr.uno.User;
import htl.steyr.uno.server.database.DatabaseConnection;
import htl.steyr.uno.server.database.DatabaseUser;
import htl.steyr.uno.server.exceptions.database.DatabaseException;
import htl.steyr.uno.server.exceptions.user.InvalidPasswordException;
import htl.steyr.uno.server.exceptions.user.UserException;
import htl.steyr.uno.server.exceptions.user.UserNotFoundException;

import java.sql.SQLException;

public class TestDBConnection {

    public static void main(String[] args) {
        DatabaseUser dbUser = new DatabaseUser();
        try {
            DatabaseConnection.getConnection();
            System.out.println("Datenbankverbindung erfolgreich hergestellt.");
            System.out.println("------------------------------\n");


            try {
                String testUsername = "mstuebl";
                User user = null;
                user = dbUser.getUser(testUsername, "admin");
                System.out.println("Username: " + user.getUsername());
                System.out.println("Vorname: " + user.getFirstName());
                System.out.println("Nachname: " + user.getLastName());
                System.out.println("Spiele gewonnen: " + user.getGamesWon());
                System.out.println("Spiele verloren: " + user.getGamesLost());
                System.out.println("Erstellt am: " + user.getCreatedAt());
                System.out.println("Letzter Login: " + user.getLastLogin());
                System.out.println("------------------------------\n");
            } catch (UserNotFoundException e) {
                System.out.println("Fehler beim Abrufen des Benutzers: " + e.getMessage() + "\n");
            } catch (InvalidPasswordException e) {
                System.out.println("Invalid password\n");
            } catch (UserException e) {
                System.out.println("Allgemeiner Fehler: " + e.getMessage() + "\n");
            }


            User addedUser = dbUser.getUser("mkraschk", "123");
            System.out.println("Username: " + addedUser.getUsername());
            System.out.println("Vorname: " + addedUser.getFirstName());
            System.out.println("Nachname: " + addedUser.getLastName());
            System.out.println("Spiele gewonnen: " + addedUser.getGamesWon());
            System.out.println("Spiele verloren: " + addedUser.getGamesLost());
            System.out.println("Erstellt am: " + addedUser.getCreatedAt());
            System.out.println("Letzter Login: " + addedUser.getLastLogin());
            System.out.println("------------------------------\n");



        } catch (DatabaseException e) {
            System.out.println("Fehler bei der Datenbankverbindung: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}




