package htl.steyr.uno.client;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.Lobby.LobbyWaitController;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.LoginController;
import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;

import java.io.IOException;

public class Client {

    private String host = "localhost";
    private int port = 59362;
    private ClientSocketConnection conn;
    private final LoginController loginController;
    private LobbyController lobbyController;
    private LobbyWaitController lobbyWaitController;
    private GameTable gameTable;

    public Client(LoginController controller) {
        this.loginController = controller;
    }

    public void start() {
        Thread connectionThread = new Thread(() -> {
            do {
                try {
                    conn = new ClientSocketConnection(host, port, this);
                    conn.startReceiving();

                    getLoginController().readyToLogin();
                } catch (IOException e) {
                    conn = null;
                    getLoginController().cantLogin();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } while (conn == null);
        });
        connectionThread.start();
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

    public void createAccount(String username, String firstName, String lastName, String email, String password) {
        CreateAccountRequest msg = new CreateAccountRequest(username, firstName, lastName, email, password);
        getConn().sendMessage(msg);
    }

    public void verifyNewAccount(String username, String firstName, String lastName, String email, String password, Integer code) {
        CreateAccountRequest msg = new CreateAccountRequest(username, firstName, lastName, email, password, code);
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

    public GameTable getGameTable() {
        return gameTable;
    }
    public void setGameTable(GameTable gameTable) {
        this.gameTable = gameTable;
    }


}




