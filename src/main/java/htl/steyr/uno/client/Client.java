package htl.steyr.uno.client;

import htl.steyr.uno.requests.client.*;

import java.io.IOException;
import java.util.Scanner;

public class Client {

    private String host = "xserv.stuebl.eu";
    private int port = 59362;
    private ClientSocketConnection conn;
    Scanner console = new Scanner(System.in);

    public void start() throws IOException {
        conn = new ClientSocketConnection(host, port, this);
        conn.startReceiving();

        System.out.println("Connected to " + host + ":" + port);
        System.out.println("---------------------------------------------");


    }

    public void logIn() {
        System.out.println("Enter username:");
        String username = console.nextLine();

        System.out.println("Enter password:");
        String password = console.nextLine();

        LoginRequest msg = new LoginRequest(username, password);
        conn.sendMessage(msg);
    }

    public void joinOrCreateLobby() {
        System.out.println("j - Join lobby");
        System.out.println("c - Create lobby");
        String choice = console.nextLine();
        if (choice.equalsIgnoreCase("j")) {
            joinLobby();
        } else if (choice.equalsIgnoreCase("c")) {
            createLobby();
        } else {
            System.out.println("Invalid choice");
            joinOrCreateLobby();
        }
    }

    public void createLobby() {
        CreateLobbyRequest msg = new CreateLobbyRequest(conn.getUser());
        conn.sendMessage(msg);
    }

    public void joinLobby() {
        System.out.println("Enter lobby ID:");
        String id = console.nextLine();
        Integer lobbyId = Integer.parseInt(id);
        if (lobbyId < 1) {
            System.out.println("Invalid lobby ID");
            joinLobby();
            return;
        }
        JoinLobbyRequest msg = new JoinLobbyRequest(lobbyId);
        conn.sendMessage(msg);
    }

    public void createAccount(String username, String firstName, String lastName, String password) {
        CreateAccountRequest msg = new CreateAccountRequest(username, firstName, lastName, password);
        conn.sendMessage(msg);
    }

    public ClientSocketConnection getConn() {
        return conn;
    }

}
