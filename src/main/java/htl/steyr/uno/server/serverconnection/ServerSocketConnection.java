package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.User;
import htl.steyr.uno.client.Message;
import htl.steyr.uno.server.database.DatabaseUser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

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

                    if (obj instanceof Message) {
                        Message msg = (Message) obj;
                        String username = msg.message;
                        DatabaseUser db = new DatabaseUser();
                        User user = db.getUser(username, "admin");

                        sendMessage(user);
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




