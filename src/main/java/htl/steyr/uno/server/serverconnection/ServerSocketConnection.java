package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;
import htl.steyr.uno.server.database.DatabaseUser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class ServerSocketConnection {

    private final Server server;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receivethread;
    private boolean running;
    private User user;


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

                    if (obj instanceof LoginRequest) {
                        loginRequest((LoginRequest) obj);
                    } else if (obj instanceof CreateAccountRequest) {
                        createAccountRequest((CreateAccountRequest) obj);
                    } else if (obj instanceof CreateLobbyRequest) {
                        createLobbyRequest((CreateLobbyRequest) obj);
                    } else if (obj instanceof JoinLobbyRequest) {
                        joinLobbyRequest((JoinLobbyRequest) obj);
                    }

                }
            } catch (Exception ignored) {
            } finally {
                running = false;
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
        user = db.getUser(request.getUsername(), request.getPassword());
        Object msg;
        if (user == null) {
            msg = new LoginFailedResponse();
        } else {
            msg = new LoginSuccessResponse(user);
        }
        sendMessage(msg);
        sendLogMessage(msg);
    }


    /**
     * The createAccountRequest method handles the account creation process for a client by adding a new user to the database based on the provided information.
     * It takes a CreateAccountRequest object as a parameter, which contains the username, last name, first name, and password submitted by the client for account creation.
     * The method creates a new User object using the information from the request and then interacts with the DatabaseUser class to add the new user to the database.
     * After successfully adding the user, it retrieves the created user information from the database and sends it back to the client as a response.
     * If any SQLException occurs during this process, it is thrown to be handled by the calling method or higher-level exception handling mechanisms.
     * @param request
     * @throws SQLException
     */
    private void createAccountRequest(CreateAccountRequest request) throws SQLException {
        User user = new User(request.getUsername(), request.getLastName(), request.getFirstName(), request.getPassword());
        DatabaseUser db = new DatabaseUser();
        db.addUser(user);
        User createdUser = db.getUser(request.getUsername(), request.getPassword());
        sendMessage(new CreateAccountSuccessResponse(createdUser));
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


    public User getUser() {
        return new User(user.getUsername(), user.getLastName(), user.getFirstName());
    }


}




