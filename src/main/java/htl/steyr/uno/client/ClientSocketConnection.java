package htl.steyr.uno.client;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
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
                        case LoginSuccessResponse msg -> logInSuccessResponse(msg);
                        case LoginFailedResponse msg -> logInFailedResponse(msg);
                        case LobbyInfoResponse msg -> lobbyInfoResponse(msg);
                        case LobbyNotFoundResponse msg -> lobbyNotFoundResponse(msg);
                        case LobbyJoinRefusedResponse msg -> lobbyJoinRefusedResponse(msg);
                        case CreateAccountSuccessResponse msg -> createAccountSuccessResponse(msg);
                        case ReceiveChatMessageResponse msg -> receiveChatMessageResponse(msg);
                        case ForgotPasswordResponse msg -> forgotPasswordResponse(msg);
                        case CheckIfUserAlreadyExistsResponse msg -> checkIfUserAlreadyExistsResponse(msg);
                        case StartGameResponse msg -> startGameResponse(msg);
                        case CreateAccountFailedResponse msg -> createAccountFailedResponse(msg);
                        case CardAddResponse msg -> cardAddResponse(msg);
                        case PlayerGetResponse msg -> playerGetResponse(msg);
                        case StackInfoResponse msg -> stackInfoResponse(msg);
                        case UpdateEnemyResponse msg -> updateEnemyResponse(msg);
                        case GameTurnResponse msg -> gameTurnResponse(msg);
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
     * Start of the response handler methods.
     * <p>
     * Each method corresponds to a specific type of response from the server and is responsible
     * for processing that response and updating the client state accordingly.
     * These methods are called from the receive thread when a message of the corresponding type is received from the server.
     */




    private void logInSuccessResponse(LoginSuccessResponse msg) {
        this.user = msg.user();
        client.getLoginController().logInSuccess(user);
    }

    private void logInFailedResponse(LoginFailedResponse msg) {
        client.getLoginController().logInFailed(msg);
    }

    private void lobbyInfoResponse(LobbyInfoResponse msg) {
        this.lobby = msg;

        if (msg.users() != null) {
            if (client.getLobbyWaitController() != null) {
                client.getLobbyWaitController().setLobby(msg);
            } else if (client.getLobbyController() != null) {
                client.getLobbyController().createOrJoinPartySuccess(msg);
            }
        } else {
            System.out.println("Lobby operation failed.");
        }
    }

    private void lobbyNotFoundResponse(LobbyNotFoundResponse msg) {
        client.getLobbyController().lobbyNotFound();
    }

    private void lobbyJoinRefusedResponse(LobbyJoinRefusedResponse msg) {
        client.getLobbyController().joinPartyFailed(msg);
    }

    private void createAccountSuccessResponse(CreateAccountSuccessResponse msg) {
        client.getLoginController().createAccountSuccess(msg);
    }

    private void createAccountFailedResponse(CreateAccountFailedResponse msg) {
        client.getLoginController().createAccountFailedResponse(msg);
    }

    private void receiveChatMessageResponse(ReceiveChatMessageResponse msg) {
        if (client.getLobbyWaitController() != null) {
            client.getLobbyWaitController().receiveChatMessage(msg);
        } else if (client.getLobbyController() != null) {
            client.getLobbyController().receiveChatMessage(msg);
        }
    }

    private void forgotPasswordResponse(ForgotPasswordResponse msg) {
        client.getLoginController().forgotPasswordResponse(msg);
    }

    private void checkIfUserAlreadyExistsResponse(CheckIfUserAlreadyExistsResponse msg) {
        client.getLoginController().checkIfUserAlreadyExistsResponse(msg);
    }

    private void startGameResponse(StartGameResponse msg) throws IOException {
        client.getLobbyWaitController().startGameResponse(msg);
    }

    private void cardAddResponse(CardAddResponse msg) {
        client.getGameTable().getGameLogic().cardAddResponse(msg);
    }

    private void playerGetResponse(PlayerGetResponse msg) {
        client.getGameTable().getGameLogic().playerGetResponse(msg);
    }

    private void stackInfoResponse(StackInfoResponse msg) {
        client.getGameTable().getGameLogic().stackInfoResponse(msg);
    }

    private void updateEnemyResponse(UpdateEnemyResponse msg) {
        client.getGameTable().getGameLogic().updateEnemyResponse(msg);
    }

    private void gameTurnResponse(GameTurnResponse msg) {
        client.getGameTable().getGameLogic().gameTurnResponse(msg);
    }




    /**
     * End of the response handler methods.
     */





    /**
     * Start of the request sending methods.
     * <p>
     * Each method corresponds to a specific type of request that the client can send to the server,
     * such as requesting a password reset, verifying a password reset code, setting a new password,
     * checking if a user already exists, and starting a game.
     * These methods create the appropriate request object and send it to the server using the sendMessage method.
     * They are typically called from the client UI when the user performs an action that requires communication with the server,
     * such as clicking a button to reset their password or start a game.
     */


    public void requestPasswordReset(String email) {
        ForgotPasswordRequest msg = new ForgotPasswordRequest(email);
        sendMessage(msg);
    }

    public void verifyPasswordResetCode(int code) {
        ForgotPasswordSendCodeRequest msg = new ForgotPasswordSendCodeRequest(code);
        sendMessage(msg);
    }

    public void setNewPassword(String email, int code, String password) {
        ChangePasswordRequest msg = new ChangePasswordRequest(email, code, password);
        sendMessage(msg);
    }

    public void checkIfUserAlreadyExists(String username, String email) {
        CheckIfUserAlreadyExistsRequest msg = new CheckIfUserAlreadyExistsRequest(username, email);
        sendMessage(msg);
    }

    public void startGame() {
        StartGameRequest msg = new StartGameRequest(getUser());
        sendMessage(msg);
    }

    public void setProfileImageRequest(byte[] imageBytes) {
        SetProfileImageRequest msg = new SetProfileImageRequest(getUser(), imageBytes);
        sendMessage(msg);
    }





    /**
     * End of the request sending methods.
     */






    /**
     * Handles the logic for leaving a lobby by sending a leave lobby request to the server and updating the client state.
     * This method is typically called when the user chooses to leave the lobby, either by clicking a "Leave Lobby" button
     * or by closing the lobby window. It ensures that the server is notified of the user's departure and that the client
     * state is updated accordingly.
     */
    public void leaveLobby() {
        if (lobby != null) {
            lobby = null;
            LeaveLobbyRequest msg = new LeaveLobbyRequest(getUser());
            sendMessage(msg);
        }
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
