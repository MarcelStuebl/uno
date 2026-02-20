package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.server.database.DatabaseUser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class ServerSocketConnection{

    private final Server server;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receivethread;
    private boolean running;
    private User user;


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

    public void sendMessage(Object message) {
        try {
            out.reset();
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startReceiving() {
        running = true;
        receivethread = new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    server.getMessage("[" + socket.getRemoteSocketAddress() + "] " + obj);

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


    private void loginRequest(LoginRequest request) throws SQLException {
        DatabaseUser db = new DatabaseUser();
        user = db.getUser(request.getUsername(), request.getPassword());
        sendMessage(user);
    }

    private void createAccountRequest(CreateAccountRequest request) throws SQLException {
        User user = new User(request.getUsername(), request.getLastName(), request.getFirstName(), request.getPassword());
        DatabaseUser db = new DatabaseUser();
        db.addUser(user);
        User createdUser = db.getUser(request.getUsername(), request.getPassword());
        sendMessage(createdUser);
    }

    private void createLobbyRequest(CreateLobbyRequest obj) {
            Lobby lobby = new Lobby(server);
            lobby.addConnection(this);
            lobby.updateJoined();
    }

    private void joinLobbyRequest(JoinLobbyRequest obj) {
        Lobby lobby = server.getLobbies().stream().filter(l -> l.getLobbyId() == obj.getLobbyId()).findFirst().orElse(null);
        if (lobby != null) {
            lobby.addConnection(this);
            lobby.updateJoined();
        } else {
            sendMessage("Lobby not found");
        }
    }


    public User getUser() {
        return new User(user.getUsername(), user.getLastName(), user.getFirstName());
    }


}




