package htl.steyr.uno.client;

import htl.steyr.uno.LobbyController;
import htl.steyr.uno.LoginController;
import htl.steyr.uno.requests.client.*;

import java.io.IOException;
import java.util.Scanner;

public class Client {

    private String host = "xserv.stuebl.eu";
    private int port = 59362;
    private ClientSocketConnection conn;
    private Scanner console = new Scanner(System.in);
    private final LoginController loginController;
    private LobbyController lobbyController;

    public Client(LoginController controller) {
        this.loginController = controller;
    }

    public void start() throws IOException {
        conn = new ClientSocketConnection(host, port, this);
        conn.startReceiving();

        System.out.println("Connected to " + host + ":" + port);
        System.out.println("---------------------------------------------");
    }

    public void logIn(String username, String password) {
        LoginRequest msg = new LoginRequest(username, password);
        conn.sendMessage(msg);
    }

    public void createLobby() {
        CreateLobbyRequest msg = new CreateLobbyRequest(conn.getUser());
        conn.sendMessage(msg);
    }

    public void joinLobby(int lobbyId) {
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

    public LoginController getLoginController() {
        return loginController;
    }

    public LobbyController getLobbyController() {
        return lobbyController;
    }
    public void setLobbyController(LobbyController lobbyController) {
        this.lobbyController = lobbyController;
    }


}




