package htl.steyr.uno.client;

import htl.steyr.uno.Lobby.LobbyWaitController;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.LoginController;
import htl.steyr.uno.requests.client.CreateAccountRequest;
import htl.steyr.uno.requests.client.CreateLobbyRequest;
import htl.steyr.uno.requests.client.JoinLobbyRequest;
import htl.steyr.uno.requests.client.LoginRequest;

import java.io.IOException;

public class Client {

    private String host = "server.uno.clouddb.at";
    private int port = 59362;
    private ClientSocketConnection conn;
    private final LoginController loginController;
    private LobbyController lobbyController;
    private LobbyWaitController lobbyWaitController;

    public Client(LoginController controller) {
        this.loginController = controller;
    }

    public void start() {
        try {
            conn = new ClientSocketConnection(host, port, this);
            conn.startReceiving();

            System.out.println("Connected to " + host + ":" + port);
            System.out.println("---------------------------------------------");
        } catch (IOException e) {
            System.out.println("Failed to connect to " + host + ":" + port);
        }
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

    public LobbyWaitController getLobbyWaitController() {
        return lobbyWaitController;
    }
    public void setLobbyWaitController(LobbyWaitController lobbyWaitController) {
        this.lobbyWaitController = lobbyWaitController;
    }


}




