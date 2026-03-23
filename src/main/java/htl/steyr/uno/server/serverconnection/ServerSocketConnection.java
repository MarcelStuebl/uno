package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.LobbyController;
import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import htl.steyr.uno.server.MailSender;
import htl.steyr.uno.server.database.DatabaseUser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.security.SecureRandom;

public class ServerSocketConnection {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Server server;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receivethread;
    private boolean running;
    private User user;
    private PasswordForgotten passwordForgotten;
    private Integer createAccountCode;


    /**
     * The constructor for the ServerSocketConnection class initializes the connection with the client by setting up input and output streams for communication.
     * It takes a Socket object representing the client's connection and a reference to the Server instance as parameters.
     * The constructor creates an ObjectOutputStream for sending messages to the client and an ObjectInputStream for receiving messages from the client.
     * If an IOException occurs during the setup of the streams, it throws a RuntimeException.
     * @param socket
     * @param server
     */
    public ServerSocketConnection(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error setting up connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * The sendMessage method is responsible for sending a message to the client through the output stream.
     * It takes an Object as a parameter, which represents the message to be sent.
     * The method first resets the output stream to ensure that any previous messages are cleared, then it writes the message object to the stream and flushes it to ensure that the message is sent immediately
     * If an IOException occurs during the process of sending the message, it throws a RuntimeException.
     * @param message
     */
    public void sendMessage(Object message) {
        try {
            out.reset();
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * The sendLogMessage method is a helper method that formats and sends a log message to the server's logging system.
     * It takes an Object as a parameter, which represents the message to be logged.
     * The method constructs a log message by combining the client's remote socket address with the provided message, and then it calls the server's setLogMessage method to update the log.
     * This allows for tracking client interactions and activities on the server side.
     * @param msg
     */
    private void sendLogMessage(Object msg) {
        server.setLogMessage("[" + socket.getRemoteSocketAddress() + "] " + msg);
    }


    /**
     * The startReceiving method initiates a separate thread to continuously listen for incoming messages from the client.
     * It sets the running flag to true and creates a new thread that reads objects from the input stream in a loop.
     * Depending on the type of the received object, it calls the appropriate handler method (e.g., loginRequest, createAccountRequest, createLobbyRequest, ...) to process the request.
     * If an exception occurs while reading from the input stream, it is caught and ignored, and the running flag is set to false to stop the thread.
     */
    public void startReceiving() {
        running = true;
        receivethread = new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    sendLogMessage(obj);

                    switch (obj) {
                        case LoginRequest msg -> loginRequest(msg);
                        case CreateAccountRequest msg -> createAccountRequest(msg);
                        case CreateLobbyRequest msg -> createLobbyRequest(msg);
                        case JoinLobbyRequest msg -> joinLobbyRequest(msg);
                        case LeaveLobbyRequest msg -> leftLobbyRequest(msg);
                        case SendChatMessageRequest msg -> sendChatMessageRequest(msg);
                        case ForgotPasswordRequest msg -> forgotPasswordRequest(msg);
                        case ForgotPasswordSendCodeRequest msg -> forgotPasswordSendCodeRequest(msg);
                        case ChangePasswordRequest msg -> changePasswordRequest(msg);
                        case CheckIfUserAlreadyExistsRequest msg -> checkIfUserAlreadyExistsRequest(msg);
                        case StartGameRequest msg -> startGameRequest(msg);
                        case null, default -> System.out.println("Received unknown message: " + obj);
                    }

                }
            } catch (Exception ignored) {
            } finally {
                running = false;
                server.removeConnection(this);
            }
        });
        receivethread.start();
    }




    /**
     * The loginRequest method handles the login process for a client by validating the provided username and password against the database.
     * It takes a LoginRequest object as a parameter, which contains the username and password submitted by the client.
     * The method creates an instance of the DatabaseUser class to interact with the user database and retrieves the user information based on the provided credentials.
     * If the user is found (i.e., the credentials are valid), it sends a LoginSuccessResponse back to the client with the user information.
     * If the user is not found (i.e., the credentials are invalid), it sends a LoginFailedResponse back to the client.
     * The method also logs the outcome of the login attempt using the sendLogMessage method to provide feedback on the server side.
     * @param request
     * @throws SQLException
     */
    private void loginRequest(LoginRequest request) throws SQLException {
        DatabaseUser db = new DatabaseUser();
        user = db.getUserPerUserName(request.getUsername(), request.getPassword());
        Object msg;
        if (user == null) {
            msg = new LoginFailedResponse();
        } else {
            msg = new LoginSuccessResponse(user);
        }
        sendMessage(msg);
        sendLogMessage(msg);
    }



    private void createAccountRequest(CreateAccountRequest request) throws SQLException {
        if (request.getCode() == null || createAccountCode == null) {
            createAccountCode = SECURE_RANDOM.nextInt(900000) + 100000; // Generate a random 6-digit code
            MailSender ms = new MailSender();
            ms.sendAuthenticationCode(request.getEmail(), createAccountCode.toString());
        } else {
            if (!request.getCode().equals(createAccountCode)) {
                sendMessage(new CreateAccountFailedResponse());
            } else {
                User user = new User(request.getUsername(), request.getLastName(), request.getFirstName(), request.getEmail(), request.getPassword());
                DatabaseUser db = new DatabaseUser();
                db.addUser(user);
                User createdUser = db.getUserPerUserName(request.getUsername(), request.getPassword());
                sendMessage(new CreateAccountSuccessResponse(createdUser));
            }
        }
    }


    /**
     * The createLobbyRequest method handles the process of creating a new lobby for the client.
     * It takes a CreateLobbyRequest object as a parameter, which contains the necessary information for creating a lobby.
     * The method creates a new instance of the Lobby class, passing the server reference to it.
     * It then adds the current connection (this) to the newly created lobby and sends a CreateLobbySuccessResponse back to the client to indicate that the lobby has been successfully created.
     * The method also logs the creation of the lobby using the sendLogMessage method and updates the lobby information for all connected clients by calling the updateJoined method on the lobby instance.
     * @param obj
     */
    private void createLobbyRequest(CreateLobbyRequest obj) {
        Lobby lobby = new Lobby(server);
        lobby.addConnection(this);
        CreateLobbySuccessResponse msg = new CreateLobbySuccessResponse();
        sendMessage(msg);
        sendLogMessage(msg);
        lobby.updateJoined();
    }


    /**
     * The joinLobbyRequest method handles the process of a client attempting to join an existing lobby.
     * It takes a JoinLobbyRequest object as a parameter, which contains the lobby ID that the client wishes to join.
     * The method searches for the lobby with the specified ID in the server's list of lobbies. If the lobby is found and can be joined (i.e., it is not full), it adds the current connection (this) to the lobby and updates the lobby information for all connected clients.
     * If the lobby is found but cannot be joined (i.e., it is full), it sends a LobbyJoinRefusedResponse back to the client, including the lobby information in the response.
     * If the lobby is not found, it sends a LobbyNotFoundResponse back to the client.
     * The method also logs the outcome of the join attempt using the sendLogMessage method to provide feedback on the server side.
     * @param obj
     */
    private void joinLobbyRequest(JoinLobbyRequest obj) {
        Lobby lobby = server.getLobbies().stream().filter(l -> l.getLobbyId() == obj.getLobbyId()).findFirst().orElse(null);
        if (lobby != null && lobby.canJoin()) {
            lobby.addConnection(this);
            lobby.updateJoined();
        } else if (lobby != null && !lobby.canJoin()) {
            LobbyJoinRefusedResponse msg = new LobbyJoinRefusedResponse(getUser(), lobby.getLobbyInfoResponse());
            sendMessage(msg);
            sendLogMessage(msg);
        } else {
            LobbyNotFoundResponse msg = new LobbyNotFoundResponse(getUser());
            sendMessage(msg);
            sendLogMessage(msg);
        }
    }

    private void sendChatMessageRequest(SendChatMessageRequest obj) {
        Lobby lobby = server.getLobbies().stream().filter(l -> l.getConnections().contains(this)).findFirst().orElse(null);
        if (lobby != null) {
            lobby.sendChatMessage(obj);
        }
    }

    private void forgotPasswordRequest(ForgotPasswordRequest msg) throws SQLException {
        // Check if there is an existing password reset request for the provided email and if it is still valid (within 1 minute)
        if (passwordForgotten == null || passwordForgotten.getRequestTime().getTime() + 60000 < System.currentTimeMillis()) {

            User user = new DatabaseUser().getUserPerEmail(msg.getEmail());
            if (user.getUsername() != null) {
                Integer code = Integer.parseInt(String.format("%06d", new java.util.Random().nextInt(1000000)));
                passwordForgotten = new PasswordForgotten(code, new Timestamp(System.currentTimeMillis()));
                MailSender ms = new MailSender();
                ms.sendAuthenticationCode(msg.getEmail(), passwordForgotten.getCode().toString());
                sendMessage(new ForgotPasswordResponse(0));
            } else {
                sendMessage(new ForgotPasswordResponse(6));
            }

        } else {
            sendMessage(new ForgotPasswordResponse(1));
        }

    }

    private void forgotPasswordSendCodeRequest(ForgotPasswordSendCodeRequest msg) {
        if (passwordForgotten != null && passwordForgotten.getCode().equals(msg.getCode())) {
            // Code is correct, allow the user to enter a new password
            sendMessage(new ForgotPasswordResponse(3));
        } else {
            sendMessage(new ForgotPasswordResponse(2));
        }
    }

    private void changePasswordRequest(ChangePasswordRequest msg) throws SQLException {
        if (passwordForgotten.getCode().equals(msg.getCode())) {
            DatabaseUser db = new DatabaseUser();

            User user = db.getUserPerEmail(msg.getEmail());
            db.updatePassword(user.getUsername(), msg.getNewPassword());

            ForgotPasswordResponse response = new ForgotPasswordResponse(4);
            sendMessage(response);
        } else {
            ForgotPasswordResponse response = new ForgotPasswordResponse(5);
            sendMessage(response);
        }
    }

    private void checkIfUserAlreadyExistsRequest(CheckIfUserAlreadyExistsRequest msg) throws SQLException {
        DatabaseUser db = new DatabaseUser();
        String username = msg.getUsername();
        String email = msg.getEmail();

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
        Lobby lobby = server.getLobbyByConnection(this);
        System.out.println(lobby);
        lobby.startGame();
    }


    private void leftLobbyRequest(LeaveLobbyRequest obj) {
        server.leaveLobby(this);
    }


    public User getUser() {
        return new User(user.getUsername(), user.getLastName(), user.getFirstName());
    }


}





