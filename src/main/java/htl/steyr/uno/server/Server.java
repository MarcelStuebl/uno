package htl.steyr.uno.server;

import htl.steyr.uno.User;
import htl.steyr.uno.server.database.DatabaseConnection;
import htl.steyr.uno.server.database.DatabaseUser;

import java.sql.SQLException;

public class Server {


    public static void main(String[] args) {
        DatabaseUser dbUser = new DatabaseUser();
        try {
            DatabaseConnection.getConnection();
            System.out.println("Datenbankverbindung erfolgreich hergestellt.");
            System.out.println("------------------------------\n");


            String testUsername = "mstuebl";
            User user = null;
            try {
                user = dbUser.getUser(testUsername, "admin");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Username: " + user.getUsername());
            System.out.println("Vorname: " + user.getFirstName());
            System.out.println("Nachname: " + user.getLastName());
            System.out.println("Spiele gewonnen: " + user.getGamesWon());
            System.out.println("Spiele verloren: " + user.getGamesLost());
            System.out.println("Erstellt am: " + user.getCreatedAt());
            System.out.println("Letzter Login: " + user.getLastLogin());
            System.out.println("------------------------------\n");




            User addedUser = dbUser.getUser("mkraschk", "123");
            System.out.println("Username: " + addedUser.getUsername());
            System.out.println("Vorname: " + addedUser.getFirstName());
            System.out.println("Nachname: " + addedUser.getLastName());
            System.out.println("Spiele gewonnen: " + addedUser.getGamesWon());
            System.out.println("Spiele verloren: " + addedUser.getGamesLost());
            System.out.println("Erstellt am: " + addedUser.getCreatedAt());
            System.out.println("Letzter Login: " + addedUser.getLastLogin());
            System.out.println("------------------------------\n");



        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}




