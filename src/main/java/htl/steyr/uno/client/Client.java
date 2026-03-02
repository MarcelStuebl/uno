package htl.steyr.uno.client;

import htl.steyr.uno.Lobby.LobbyWaitController;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.LoginController;
import htl.steyr.uno.requests.client.*;

import java.io.IOException;

public class Client {

    private String host = "localhost";
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
        getConn().sendMessage(msg);
    }

    public void createLobby() {
        CreateLobbyRequest msg = new CreateLobbyRequest(getConn().getUser());
        getConn().sendMessage(msg);
    }

    public void joinLobby(int lobbyId) {
        JoinLobbyRequest msg = new JoinLobbyRequest(lobbyId);
        getConn().sendMessage(msg);
    }

    public void createAccount(String username, String firstName, String lastName, String password) {
        CreateAccountRequest msg = new CreateAccountRequest(username, firstName, lastName, password);
        getConn().sendMessage(msg);
    }

    public void sendChatMessage(String message) {
        SendChatMessageRequest msg = new SendChatMessageRequest(message, getConn().getUser());
        getConn().sendMessage(msg);
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




