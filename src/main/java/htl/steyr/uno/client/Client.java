package htl.steyr.uno.client;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.Lobby.LobbyWaitController;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.LoginController;
import htl.steyr.uno.requests.client.*;
import javafx.application.Platform;

import java.io.IOException;

public class Client {

    private final String host = "servere.uno.clouddb.at";
    private final int port = 59362;

    private volatile ClientSocketConnection conn;
    private final LoginController loginController;
    private LobbyController lobbyController;
    private LobbyWaitController lobbyWaitController;
    private GameTable gameTable;

    private volatile boolean running = true;
    private Thread connectionThread;

    public Client(LoginController controller) {
        this.loginController = controller;
    }

    /**
     * Initializes the connection to the server in a separate background thread.
     * This method is called when the application starts to establish the connection to the server.
     * 
     * The method:
     * 1. Checks if a connection thread is already running to prevent multiple connection attempts
     * 2. Sets the running flag to true
     * 3. Creates a new background thread that attempts to establish a socket connection
     * 4. Retries connection every 5 seconds if the initial attempt fails
     * 5. Once connected, starts the receive thread to listen for server messages
     * 6. Notifies the UI when the connection is ready via the login controller
     * 
     * The connection thread:
     * - Attempts to create a new ClientSocketConnection to the server
     * - If successful, starts the receive thread and notifies the UI
     * - If failed, waits 5 seconds and retries (unless the application is shutting down)
     * - Continues until a connection is successfully established
     * - Is marked as daemon so it doesn't prevent application shutdown
     * 
     * This design ensures resilient connection handling and allows the user interface
     * to start immediately even if the server is temporarily unavailable.
     */
    public void start() {
        if (connectionThread != null && connectionThread.isAlive()) {
            return;
        }

        running = true;

        connectionThread = new Thread(() -> {
            while (running && conn == null) {
                try {
                    conn = new ClientSocketConnection(host, port, this);

                    if (!running) {
                        closeConnection();
                        return;
                    }

                    conn.startReceiving();

                    Platform.runLater(() -> {
                        if (running) {
                            getLoginController().readyToLogin();
                        }
                    });

                    return;
                } catch (IOException e) {
                    conn = null;

                    Platform.runLater(() -> {
                        if (running) {
                            getLoginController().cantLogin();
                        }
                    });

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    public synchronized void shutdown() {
        running = false;
        closeConnection();

        if (connectionThread != null) {
            connectionThread.interrupt();
            connectionThread = null;
        }

        Platform.runLater(Platform::exit);
    }

    private synchronized void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignored) {
            } finally {
                conn = null;
            }
        }
    }

    public synchronized void logIn(String username, String password) {
        if (getConn() == null) return;
        LoginRequest msg = new LoginRequest(username, password);
        getConn().sendMessage(msg);
    }

    public synchronized void createLobby() {
        if (getConn() == null) return;
        CreateLobbyRequest msg = new CreateLobbyRequest(getConn().getUser());
        getConn().sendMessage(msg);
    }

    public synchronized void joinLobby(int lobbyId) {
        if (getConn() == null) return;
        JoinLobbyRequest msg = new JoinLobbyRequest(lobbyId);
        getConn().sendMessage(msg);
    }

    public synchronized void createAccount(String username, String firstName, String lastName, String email, String password) {
        if (getConn() == null) return;
        CreateAccountRequest msg = new CreateAccountRequest(username, firstName, lastName, email, password);
        getConn().sendMessage(msg);
    }

    public void verifyNewAccount(String username, String firstName, String lastName, String email, String password, Integer code) {
        if (getConn() == null) return;
        CreateAccountRequest msg = new CreateAccountRequest(username, firstName, lastName, email, password, code);
        getConn().sendMessage(msg);
    }

    public void sendChatMessage(String message) {
        if (getConn() == null) return;
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