package htl.steyr.uno.client;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.server.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocketConnection implements Closeable {

    private final Client client;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receiveThread;
    private volatile boolean running;
    private User user;
    private LobbyInfoResponse lobby;


    /**
     * Establishes a socket connection to the server and initializes input/output streams.
     *
     * @param host   the server's hostname or IP address
     * @param port   the server's port number
     * @param client the client instance to handle callbacks
     * @throws IOException if an I/O error occurs when creating the socket or streams
     */
    public ClientSocketConnection(String host, int port, Client client) throws IOException {
        this.socket = new Socket(host, port);
        this.client = client;

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }


    /**
     * Sends a message to the server by writing an object to the output stream.
     *
     * @param message the message object to send
     * @throws RuntimeException if an I/O error occurs while sending the message
     */
    public void sendMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Starts a background thread to continuously receive messages from the server.
     * The thread will process incoming messages and invoke appropriate callbacks on the client.
     */
    public void startReceiving() {
        running = true;
        receiveThread = new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();

                    /*
                     * The following block checks the type of the received message and calls the corresponding
                     * handler method on the client. Each handler method is responsible for processing a specific
                     * type of response from the server, such as login success, login failure, lobby information,
                     * etc. If an unknown message type is received, it will print a message to the console.
                     */
                    switch (obj) {
                        case LoginSuccessResponse msg -> logInSuccess(msg);
                        case LoginFailedResponse msg -> logInFailed(msg);
                        case LobbyInfoResponse msg -> gotLobby(msg);
                        case JoinLobbySuccessResponse msg -> joinLobbySuccess(msg);
                        case LobbyNotFoundResponse msg -> lobbyNotFound(msg);
                        case LobbyJoinRefusedResponse msg -> lobbyJoinRefused(msg);
                        case CreateLobbySuccessResponse msg -> createLobbySuccess(msg);
                        case CreateAccountSuccessResponse msg -> createAccountSuccess(msg);
                        case null, default -> System.out.println("Received unknown message: " + obj);
                    }


                }
            } catch (Exception e) {
                if (running) System.out.println("Receive error: " + e.getMessage());
            }
        });
        receiveThread.start();
    }


    /**
     * Handles a successful login response from the server by updating the user information
     * and prompting the client to join or create a lobby.
     *
     * @param msg the login success response containing user information
     */
    private void logInSuccess(LoginSuccessResponse msg) {
        this.user = msg.getUser();
        client.getLoginController().logInSuccess(user);
    }


    /**
     * Handles a failed login response from the server by notifying the user and prompting
     * them to try logging in again.
     *
     * @param msg the login failed response containing error information
     */
    private void logInFailed(LoginFailedResponse msg) {
        client.getLoginController().logInFailed(msg);
    }


    /**
     * Handles a lobby information response from the server by updating the lobby state
     * and displaying the lobby details. If the lobby information is invalid, it prompts
     * the client to join or create a lobby again.
     *
     * @param lobby the lobby information response containing details about the lobby
     */
    private void gotLobby(LobbyInfoResponse lobby) {
        this.lobby = lobby;

        if (lobby.getUsers() != null) {
            if (client.getLobbyWaitController() != null) {
                client.getLobbyWaitController().setLobby(lobby);
            } else if (client.getLobbyController() != null) {
                client.getLobbyController().createOrJoinPartySuccess(lobby);
            }
        } else {
            System.out.println("Lobby operation failed.");
        }
    }


    /**
     * Handles a lobby not found response from the server by notifying the user and prompting
     * them to join or create a lobby again.
     *
     * @param lobby the lobby not found response indicating that the specified lobby does not exist
     */
    private void lobbyNotFound(LobbyNotFoundResponse lobby) {
        client.getLobbyController().lobbyNotFound();
    }


    /**
     * Handles a lobby join refused response from the server by checking the status of the lobby
     * and notifying the user accordingly. It then prompts the client to join or create a lobby again.
     *
     * @param msg the lobby join refused response containing information about why the join request was refused
     */
    private void lobbyJoinRefused(LobbyJoinRefusedResponse msg) {
        if (msg.getLobbyInfo().getStatus() == 1) {
            System.out.println("Lobby is full. Please try again.");
        } else if (msg.getLobbyInfo().getStatus() == 2) {
            System.out.println("Game already started. Please try again.");
        } else {
            System.out.println("Unknown error. Please try again.");
        }
    }


    /**
     * Handles a successful lobby creation response from the server by notifying the user.
     *
     * @param msg the create lobby success response indicating that the lobby was created successfully
     */
    private void createLobbySuccess(CreateLobbySuccessResponse msg) {
        System.out.println("Lobby created successfully.");
    }


    /**
     * Handles a successful lobby join response from the server by notifying the user.
     *
     * @param msg the join lobby success response indicating that the lobby was joined successfully
     */
    private void joinLobbySuccess(JoinLobbySuccessResponse msg) {
        System.out.println("Joined lobby successfully.");
    }


    private void createAccountSuccess(CreateAccountSuccessResponse msg) {
        client.getLoginController().createAccountSuccess(msg);
    }


    /**
     * Closes the socket connection and stops the receiving thread. This method is called when the client
     * is shutting down or when an error occurs that requires closing the connection.
     */
    @Override
    public void close() {
        running = false;
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }

    public User getUser() {
        return user;
    }


}
