package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.User;
import htl.steyr.uno.client.requests.CreateAccountRequest;
import htl.steyr.uno.client.requests.LoginRequest;
import htl.steyr.uno.server.database.DatabaseUser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerSocketConnection implements PublisherInterface{

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private Thread receivethread;
    private boolean running;
    private final List<SubscriberInterface> subscribers = Collections.synchronizedList(new ArrayList<>());


    public ServerSocketConnection(Socket socket) {
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
                    notifySubscribers(obj);

                    if (obj instanceof LoginRequest) {
                        LoginRequest request = (LoginRequest) obj;
                        DatabaseUser db = new DatabaseUser();
                        User user = db.getUser(request.getUsername(), request.getPassword());

                        sendMessage(user);
                    } else if (obj instanceof CreateAccountRequest) {
                        CreateAccountRequest request = (CreateAccountRequest) obj;
                        User user = new User(request.getUsername(), request.getLastName(), request.getFirstName(), request.getPassword());
                        DatabaseUser db = new DatabaseUser();
                        db.addUser(user);
                        User createdUser = db.getUser(request.getUsername(), request.getPassword());
                        sendMessage(createdUser);
                    }

                }
            } catch (Exception ignored) {
            } finally {
                running = false;
            }
        });
        receivethread.start();
    }


    @Override
    public void addSubscriber(SubscriberInterface subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void notifySubscribers(Object message) {
        for (SubscriberInterface subscriber : subscribers) {
            subscriber.notify(new Event(this, message));
        }
    }
}




