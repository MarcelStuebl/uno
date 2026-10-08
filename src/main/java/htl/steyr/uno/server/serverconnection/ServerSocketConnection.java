package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import htl.steyr.uno.server.database.DatabaseLog;
import htl.steyr.uno.server.database.DatabaseUser;
import htl.steyr.uno.server.exceptions.database.UserAlreadyExistsException;
import htl.steyr.uno.protocol.JsonMessageCodec;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ServerSocketConnection {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Server server;
    private final WebSocket webSocket;
    private volatile boolean running;
    private User user;
    private PasswordForgotten passwordForgotten;
    private Integer createAccountCode;


    /**
     * Attaches a WebSocket connection to the server-side request dispatcher.
     *
     * @param webSocket client WebSocket
     * @param server server instance
     */
    public ServerSocketConnection(WebSocket webSocket, Server server) {
        this.server = server;
        this.webSocket = webSocket;
    }


    /** Sends an encoded JSON message to the client. */
    public synchronized void sendMessage(Object message) {
        if (!running) {
            return;
        }
        try {
            webSocket.send(JsonMessageCodec.encode(message));
        } catch (IOException e) {
            handleDisconnection("Send error: " + e.getMessage());
        }
    }
    
    /**
     * Internal method to handle disconnection gracefully.
     * This method ensures that when a connection error occurs (e.g., IOException during message sending),
     * the connection is properly closed and cleaned up without throwing exceptions.
     * It sets the running flag to false, notifies the server to remove this connection from its list,
     * removes the connection from any active lobby, and closes the socket.
     * This prevents zombie connections and ensures that other players in the lobby are notified of the disconnection.
     *
     * @param reason a descriptive string explaining why the disconnection occurred
     */
    private void handleDisconnection(String reason) {
        if (!running) {
            return;
        }
        
        running = false;
        sendLogMessage("Disconnecting - Reason: " + reason);
        
        try {
            server.removeConnection(this);
        } catch (Exception e) {
            System.out.println("Error during removeConnection: " + e.getMessage());
        }
        
        if (webSocket.isOpen()) {
            webSocket.close();
        }
    }


    /**
     * The sendLogMessage method is a helper method that formats and sends a log message to the server's logging system.
     * It takes an Object as a parameter, which represents the message to be logged.
     * The method constructs a log message by combining the client's remote socket address with the provided message, and then it calls the server's sendLogMessage method to update the log.
     * This allows for tracking client interactions and activities on the server side for debugging and monitoring purposes.
     *
     * @param msg the message object to be logged
     */
    private void sendLogMessage(Object msg) {
        server.sendLogMessage("[" + remoteAddress() + "] " + msg);
    }


    /**
     * The startReceiving method initiates a separate thread to continuously listen for incoming messages from the client.
     * It sets the running flag to true and creates a new thread that reads objects from the input stream in a loop.
     * Depending on the type of the received object, it calls the appropriate handler method (e.g., loginRequest, createAccountRequest, createLobbyRequest, ...) to process the request.
     * If an exception occurs while reading from the input stream, it is caught and the connection is properly closed.
     */
    public void startReceiving() {
        running = true;
        startHeartbeat();
    }

    /**
     * Decodes one WebSocket text frame and dispatches it to the existing
     * request handlers. No Java class names or serialized object bytes cross
     * the network.
     */
    public void onMessage(String json) {
        if (!running) {
            return;
        }
        try {
            JsonMessageCodec.DecodedMessage message = JsonMessageCodec.decode(json);
            switch (message.type()) {
                case "LoginRequest" -> loginRequest(JsonMessageCodec.convert(message, LoginRequest.class));
                case "CreateAccountRequest" -> createAccountRequest(JsonMessageCodec.convert(message, CreateAccountRequest.class));
                case "CreateLobbyRequest" -> createLobbyRequest(JsonMessageCodec.convert(message, CreateLobbyRequest.class));
                case "JoinLobbyRequest" -> joinLobbyRequest(JsonMessageCodec.convert(message, JoinLobbyRequest.class));
                case "LeaveLobbyRequest" -> leftLobbyRequest(JsonMessageCodec.convert(message, LeaveLobbyRequest.class));
                case "SendChatMessageRequest" -> sendChatMessageRequest(JsonMessageCodec.convert(message, SendChatMessageRequest.class));
                case "ForgotPasswordRequest" -> forgotPasswordRequest(JsonMessageCodec.convert(message, ForgotPasswordRequest.class));
                case "ForgotPasswordSendCodeRequest" -> forgotPasswordSendCodeRequest(JsonMessageCodec.convert(message, ForgotPasswordSendCodeRequest.class));
                case "ChangePasswordRequest" -> changePasswordRequest(JsonMessageCodec.convert(message, ChangePasswordRequest.class));
                case "CheckIfUserAlreadyExistsRequest" -> checkIfUserAlreadyExistsRequest(JsonMessageCodec.convert(message, CheckIfUserAlreadyExistsRequest.class));
                case "StartGameRequest" -> startGameRequest(JsonMessageCodec.convert(message, StartGameRequest.class));
                case "CardPlayedRequest" -> cardPlayedRequest(JsonMessageCodec.convert(message, CardPlayedRequest.class));
                case "ReadyInGameTableRequest" -> readyInGameTableRequest(JsonMessageCodec.convert(message, ReadyInGameTableRequest.class));
                case "RequestCardRequest" -> requestCardRequest(JsonMessageCodec.convert(message, RequestCardRequest.class));
                case "SayUnoRequest" -> sayUnoRequest(JsonMessageCodec.convert(message, SayUnoRequest.class));
                case "SetProfileImageRequest" -> setProfileImageRequest(JsonMessageCodec.convert(message, SetProfileImageRequest.class));
                case "HeartbeatPingRequest" -> heartbeatPingRequest(JsonMessageCodec.convert(message, HeartbeatPingRequest.class));
                default -> System.out.println("Unknown UNO message type from " + remoteAddress() + ": " + message.type());
            }
        } catch (Exception e) {
            System.out.println("Invalid UNO message from " + remoteAddress() + ": " + e.getMessage());
            closeWithReason("Invalid message");
        }
    }
    
    /**
     * Cleanup method to ensure the connection is properly closed and removed from server.
     * This method is called in the finally block of the receive thread to guarantee that cleanup operations
     * are executed even if an exception occurs during message processing.
     * It ensures that:
     * 1. The running flag is set to false to stop any ongoing operations
     * 2. The connection is removed from the server's connection list and its lobby
     * 3. The socket is properly closed to free network resources
     * 
     * This prevents resource leaks and ensures that the connection is completely terminated.
     */
    private void cleanupConnection() {
        if (user != null) {
            System.out.println("Cleaning up connection for user: " + user.getUsername());
        }
        running = false;
        try {
            server.removeConnection(this);
        } catch (Exception e) {
            System.out.println("Error removing connection: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (webSocket.isOpen()) {
            webSocket.close();
        }
    }


    /**
     * Start of the request handling methods.
     * <p>
     * Each method corresponds to a specific type of request that the server can receive from the client such as login requests,
     * account creation requests, lobby management requests, chat messages, password reset requests, and game start requests.
     */


    /**
     * The loginRequest method handles the login process for a client by validating the provided username and password against the database.
     * It takes a LoginRequest object as a parameter, which contains the username and password submitted by the client.
     * The method first checks if there is already an active connection with the same username to prevent multiple logins from different devices.
     * If such a connection exists, it sends a LoginFailedResponse back to the client with an appropriate error code and logs the event.
     * If there is no active connection with the same username, it retrieves the user information from the database using the DatabaseUser class and checks if the credentials are valid.
     * If the credentials are valid, it sends a LoginSuccessResponse back to the client with the user information.
     * If the credentials are invalid, it sends a LoginFailedResponse back to the client with an appropriate error code.
     *
     * @param request
     * @throws SQLException
     */
    private void loginRequest(LoginRequest request) throws SQLException {
        sendLogMessage(request);

        server.getClientConnections().removeIf(c -> {
            try {
                return c.getUser() != null && c.getUser().getUsername() != null &&
                       c.getUser().getUsername().equals(request.username()) &&
                       (c.getWebSocket() == null || !c.getWebSocket().isOpen() || !c.isRunning());
            } catch (Exception e) {
                return false;
            }
        });

        if (server.getClientConnections().stream().filter(c -> c.getUser() != null).anyMatch(c -> c.getUser().getUsername().equals(request.username()))) {
            LoginFailedResponse msg = new LoginFailedResponse(2);
            sendMessage(msg);
            sendLogMessage(msg);
            return;
        }

        DatabaseUser db = new DatabaseUser();
        user = db.getUserPerUserName(request.username(), request.password());
        Object msg;
        DatabaseLog dbLog = new DatabaseLog();
        if (user == null) {
            msg = new LoginFailedResponse(1);
            dbLog.logUserLogin(null, request.username(), remoteAddress(), false);
        } else {
            msg = new LoginSuccessResponse(user);
            dbLog.logUserLogin(user.getId(), request.username(), remoteAddress(), true);
        }
        sendMessage(msg);
        sendLogMessage(msg);
    }


    /**
     * The createAccountRequest method handles the account creation process for a client by validating the provided information and interacting with the database to create a new user account.
     * It takes a CreateAccountRequest object as a parameter, which contains the necessary information for creating an account, including username, email, password, and an optional verification code.
     * The method first checks if a verification code is required and if it matches the expected code. If not, it generates a new verification code and sends it to the client's email address using the MailSender class.
     * If the verification code is valid, it creates a new User object with the provided information and checks if a user with the same username already exists in the database.
     * If a user with the same username exists, it sends a CreateAccountFailedResponse back to the client indicating that the username is already taken.
     * If the username is available, it adds the new user to the database and sends a CreateAccountSuccessResponse back to the client with the created user information.
     * The method also logs the outcome of the account creation attempt using the sendLogMessage method to provide feedback on the server side.
     *
     * @param msg
     * @throws SQLException
     */
    private void createAccountRequest(CreateAccountRequest msg) throws SQLException {
        sendLogMessage(msg);
        if (msg.code() == null || createAccountCode == null) {
            createAccountCode = SECURE_RANDOM.nextInt(900000) + 100000; // Generate a random 6-digit code
            MailSender ms = new MailSender();
            ms.sendAuthenticationCode(msg.email(), createAccountCode.toString());
        } else {
            User user = new User(msg.username(), msg.lastName(), msg.firstName(), msg.email(), msg.password());
            DatabaseUser db1 = new DatabaseUser();
            if (db1.userExists(user)) {
                sendMessage(new CreateAccountFailedResponse(1));
            } else {
                if (!msg.code().equals(createAccountCode)) {
                    sendMessage(new CreateAccountFailedResponse(2));
                } else {
                    DatabaseUser db = new DatabaseUser();
                    try {
                        db.addUser(user);
                    } catch (UserAlreadyExistsException e) {
                        sendMessage(new CreateAccountFailedResponse(1));
                    }
                    User createdUser = db.getUserPerUserName(msg.username(), msg.password());
                    sendMessage(new CreateAccountSuccessResponse(createdUser));
                }
            }
        }
    }


    /**
     * The createLobbyRequest method handles the process of creating a new lobby for the client.
     * It takes a CreateLobbyRequest object as a parameter, which contains the necessary information for creating a lobby.
     * The method creates a new instance of the Lobby class, passing the server reference to it.
     * It then adds the current connection (this) to the newly created lobby and sends a CreateLobbySuccessResponse back to the client to indicate that the lobby has been successfully created.
     * The method also logs the creation of the lobby using the sendLogMessage method and updates the lobby information for all connected clients by calling the updateJoined method on the lobby instance.
     *
     * @param msg
     */
    private void createLobbyRequest(CreateLobbyRequest msg) {
        sendLogMessage(msg);
        Lobby lobby = new Lobby(server);
        lobby.addConnection(this);
        lobby.updateJoined();
    }


    /**
     * The joinLobbyRequest method handles the process of a client attempting to join an existing lobby.
     * It takes a JoinLobbyRequest object as a parameter, which contains the lobby ID that the client wishes to join.
     * The method searches for the lobby with the specified ID in the server's list of lobbies. If the lobby is found and can be joined (i.e., it is not full), it adds the current connection (this) to the lobby and updates the lobby information for all connected clients.
     * If the lobby is found but cannot be joined (i.e., it is full), it sends a LobbyJoinRefusedResponse back to the client, including the lobby information in the response.
     * If the lobby is not found, it sends a LobbyNotFoundResponse back to the client.
     * The method also logs the outcome of the join attempt using the sendLogMessage method to provide feedback on the server side.
     *
     * @param msg
     */
    private void joinLobbyRequest(JoinLobbyRequest msg) {
        sendLogMessage(msg);
        Lobby lobby = server.getLobbies().stream().filter(l -> l.getLobbyId() == msg.lobbyId()).findFirst().orElse(null);
        if (lobby != null && lobby.canJoin()) {
            lobby.addConnection(this);
            lobby.updateJoined();
        } else if (lobby != null && !lobby.canJoin()) {
            LobbyJoinRefusedResponse msg2 = new LobbyJoinRefusedResponse(getUser(), lobby.getLobbyInfoResponse());
            sendMessage(msg2);
            sendLogMessage(msg2);
        } else {
            LobbyNotFoundResponse msg2 = new LobbyNotFoundResponse(getUser());
            sendMessage(msg2);
            sendLogMessage(msg2);
        }
    }


    /**
     * The sendChatMessageRequest method handles the process of a client sending a chat message to the lobby.
     * It takes a SendChatMessageRequest object as a parameter, which contains the message that the client wants to send.
     * The method first retrieves the lobby that the current connection (this) is part of by searching through the server's list of lobbies and checking if the connection is included in any of them.
     * If the lobby is found, it calls the sendChatMessage method on the lobby instance, passing the SendChatMessageRequest object to it. This allows the lobby to broadcast the chat message to all connected clients in that lobby.
     * If the lobby is not found (i.e., the connection is not part of any lobby), the method does nothing.
     *
     * @param msg
     */
    private void sendChatMessageRequest(SendChatMessageRequest msg) {
        Lobby lobby = server.getLobbies().stream().filter(l -> l.getConnections().contains(this)).findFirst().orElse(null);
        if (lobby != null) {
            lobby.sendChatMessage(msg);
        }
    }


    /**
     * The forgotPasswordRequest method handles the process of a client requesting a password reset for their account.
     * It takes a ForgotPasswordRequest object as a parameter, which contains the email address associated with the account for which the password reset is being requested.
     * The method first checks if there is an existing password reset request for the provided email and if it is still valid (i.e., within 1 minute). If there is no valid request, it proceeds to handle the password reset request.
     * It retrieves the user information based on the provided email address using the DatabaseUser class. If a user with the specified email exists, it generates a random 6-digit code and creates a new PasswordForgotten object to store the code and the request time.
     * The method then sends an authentication code to the user's email address using the MailSender class and sends a ForgotPasswordResponse back to the client indicating that the password reset request was successful.
     * If no user with the specified email exists, it sends a ForgotPasswordResponse back to the client indicating that the email is not associated with any account.
     * If there is an existing valid password reset request, it sends a ForgotPasswordResponse back to the client indicating that a request has already been made and is still valid.
     * The method also logs the outcome of the password reset request using the sendLogMessage method to provide feedback on the server side.
     *
     * @param msg
     * @throws SQLException
     */
    private void forgotPasswordRequest(ForgotPasswordRequest msg) throws SQLException {
        sendLogMessage(msg);
        // Check if there is an existing password reset request for the provided email and if it is still valid (within 1 minute)
        if (passwordForgotten == null || passwordForgotten.getRequestTime().getTime() + 60000 < System.currentTimeMillis()) {

            User user = new DatabaseUser().getUserPerEmail(msg.email());
            if (user.getUsername() != null) {
                Integer code = Integer.parseInt(String.format("%06d", new java.util.Random().nextInt(1000000)));
                passwordForgotten = new PasswordForgotten(code, new Timestamp(System.currentTimeMillis()));
                MailSender ms = new MailSender();
                ms.sendAuthenticationCode(msg.email(), passwordForgotten.getCode().toString());
                sendMessage(new ForgotPasswordResponse(0));
            } else {
                sendMessage(new ForgotPasswordResponse(6));
            }
        } else {
            sendMessage(new ForgotPasswordResponse(1));
        }

    }


    /**
     * The forgotPasswordSendCodeRequest method handles the process of a client submitting the verification code for a password reset request.
     * It takes a ForgotPasswordSendCodeRequest object as a parameter, which contains the code submitted by the client.
     * The method checks if there is an existing password reset request and if the submitted code matches the code stored in the PasswordForgotten object.
     * If the code is correct, it sends a ForgotPasswordResponse back to the client indicating that the code is valid and allowing the user to proceed with entering a new password.
     * If the code is incorrect, it sends a ForgotPasswordResponse back to the client indicating that the code is invalid.
     * The method also logs the outcome of the code verification using the sendLogMessage method to provide feedback on the server side.
     *
     * @param msg
     */
    private void forgotPasswordSendCodeRequest(ForgotPasswordSendCodeRequest msg) {
        sendLogMessage(msg);
        if (passwordForgotten != null && passwordForgotten.getCode().equals(msg.code())) {
            // Code is correct, allow the user to enter a new password
            sendMessage(new ForgotPasswordResponse(3));
        } else {
            sendMessage(new ForgotPasswordResponse(2));
        }
    }


    /**
     * The changePasswordRequest method handles the process of a client submitting a new password for their account after successfully verifying the password reset code.
     * It takes a ChangePasswordRequest object as a parameter, which contains the email address associated with the account and the new password submitted by the client.
     * The method checks if there is an existing password reset request and if the code stored in the PasswordForgotten object matches the expected code.
     * If the code is correct, it retrieves the user information based on the provided email address using the DatabaseUser class and updates the user's password in the database with the new password provided by the client.
     * It then sends a ForgotPasswordResponse back to the client indicating that the password has been successfully changed.
     * If the code is incorrect, it sends a ForgotPasswordResponse back to the client indicating that the code is invalid and that the password change request cannot be processed.
     * The method also logs the outcome of the password change request using the sendLogMessage method to provide feedback on the server side.
     *
     * @param msg
     * @throws SQLException
     */
    private void changePasswordRequest(ChangePasswordRequest msg) throws SQLException {
        sendLogMessage(msg);
        if (passwordForgotten.getCode().equals(msg.code())) {
            DatabaseUser db = new DatabaseUser();

            User user = db.getUserPerEmail(msg.email());
            db.updatePassword(user.getUsername(), msg.newPassword());

            ForgotPasswordResponse response = new ForgotPasswordResponse(4);
            sendMessage(response);
        } else {
            ForgotPasswordResponse response = new ForgotPasswordResponse(5);
            sendMessage(response);
        }
    }


    /**
     * The checkIfUserAlreadyExistsRequest method handles the process of a client checking if a username or email address is already associated with an existing account in the database.
     * It takes a CheckIfUserAlreadyExistsRequest object as a parameter, which contains the username and email address that the client wants to check for availability.
     * The method creates an instance of the DatabaseUser class to interact with the user database and checks if there is an existing user with the provided username and email address.
     * It sets boolean flags to indicate whether the username and email already exist in the database.
     * Finally, it sends a CheckIfUserAlreadyExistsResponse back to the client with the
     * results of the checks, allowing the client to provide feedback to the user about the availability of the username and email address for account creation.
     * The method also logs the outcome of the availability checks using the sendLogMessage method to
     * provide feedback on the server side.
     *
     * @param msg
     * @throws SQLException
     */
    private void checkIfUserAlreadyExistsRequest(CheckIfUserAlreadyExistsRequest msg) throws SQLException {
        DatabaseUser db = new DatabaseUser();
        String username = msg.username();
        String email = msg.email();

        boolean userAlreadyExists = false;
        boolean emailAlreadyExists = false;

        if (username != null && !username.trim().isEmpty()) {
            userAlreadyExists = db.getUserPerUserName(username).getUsername() != null;
        }

        if (email != null && !email.trim().isEmpty()) {
            emailAlreadyExists = db.getUserPerEmail(email).getUsername() != null;
        }

        sendMessage(new CheckIfUserAlreadyExistsResponse(username, email, userAlreadyExists, emailAlreadyExists));
    }


    private void startGameRequest(StartGameRequest msg) {
        sendLogMessage(msg);
        Lobby lobby = server.getLobbyByConnection(this);
        lobby.startGame();
    }


    private void leftLobbyRequest(LeaveLobbyRequest msg) {
        sendLogMessage(msg);
        server.leaveLobby(this);
    }


    private void cardPlayedRequest(CardPlayedRequest msg) {
        Lobby lobby = server.getLobbyByConnection(this);
        if (lobby != null) {
            lobby.getGameLogic().cardPlayed(msg);
        }
    }


    private void readyInGameTableRequest(ReadyInGameTableRequest msg) {
        sendLogMessage(msg);
        Lobby lobby = server.getLobbyByConnection(this);
        if (lobby != null) {
            lobby.getGameLogic().readyInGameTable(msg);
        }
    }


    private void requestCardRequest(RequestCardRequest msg) {
        Lobby lobby = server.getLobbyByConnection(this);
        if (lobby != null) {
            lobby.getGameLogic().requestCard(msg);
        }
    }


    private void sayUnoRequest(SayUnoRequest msg) {
        Lobby lobby = server.getLobbyByConnection(this);
        if (lobby != null) {
            lobby.getGameLogic().sayUno(msg);
        }
    }


    private void setProfileImageRequest(SetProfileImageRequest msg) {
        sendLogMessage(msg);
        DatabaseUser db = new DatabaseUser();
        try {
            db.updateProfileImage(getUser(), msg.imageData());
        } catch (SQLException e) {
            System.out.println("Error updating profile image for user " + getUser().getUsername() + ": " + e.getMessage());
        }
    }

    /**
     * Handles heartbeat ping requests from the client.
     * The client sends these periodically to keep the connection alive and prevent socket timeout.
     * The server responds immediately with a pong response to confirm the connection is active.
     * 
     * @param msg the heartbeat ping request from the client
     */
    private void heartbeatPingRequest(HeartbeatPingRequest msg) {
        // Respond with pong immediately - this keeps the connection alive
        sendMessage(new HeartbeatPongResponse());
    }

    /**
     * End of the request handling methods.
     */


    public User getUser() {
        if (user == null) return null;
        return new User(user.getUsername(), user.getLastName(), user.getFirstName(), user.getProfileImageData());
    }


    /**
     * Starts a periodic heartbeat/ping thread to detect dead connections proactively.
     * This method creates and starts a daemon thread that periodically checks if the connection is still active.
     * 
     * The heartbeat mechanism works as follows:
     * 1. It sleeps for 15 seconds (HEARTBEAT_INTERVAL) - reduced for faster detection
     * 2. Every 15 seconds, it checks if the socket is still connected and not closed
     * 3. If the socket is detected as closed or disconnected, it calls handleDisconnection()
     * 4. This prevents "zombie connections" that appear active but cannot communicate
     * 
     * This is particularly useful for detecting:
     * - Abruptly disconnected clients (network cable unplugged, WiFi lost, etc.)
     * - Clients that close their connection without sending a proper disconnect message
     * - Network issues that break the connection on the OS level
     * 
     * The heartbeat thread runs as a daemon thread, so it won't prevent the JVM from shutting down.
     * It will continue running in the background until the connection is closed or the application terminates.
     */
    public void startHeartbeat() {
        Thread heartbeatThread = new Thread(() -> {
            final long HEARTBEAT_INTERVAL = 15000;
            
            while (running) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    
                    if (!running || !webSocket.isOpen()) {
                        break;
                    }
                    
                    if (!webSocket.isOpen()) {
                        handleDisconnection("Heartbeat detected closed socket");
                        break;
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    handleDisconnection("Heartbeat error: " + e.getMessage());
                    break;
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.setName("Heartbeat-" + remoteAddress());
        heartbeatThread.start();
    }

    public boolean isRunning() {
        return running;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void close() {
        cleanupConnection();
    }

    public void closeWithReason(String reason) {
        handleDisconnection(reason);
    }

    private String remoteAddress() {
        return String.valueOf(webSocket.getRemoteSocketAddress());
    }
}
