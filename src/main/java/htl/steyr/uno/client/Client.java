package htl.steyr.uno.client;

import java.io.IOException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int port = 59362;

        ClientSocketConnection conn = new ClientSocketConnection(host, port);
        conn.startReceiving();

        System.out.println("Connected to " + host + ":" + port);
        System.out.println("Type text and press Enter. /quit to exit.");

        try (Scanner console = new Scanner(System.in)) {
            while (true) {
                String line = console.nextLine();
                if (line.equalsIgnoreCase("/quit")) break;

                Message msg = new Message(line);
                conn.sendMessage(msg);
            }
        }

        conn.close();
        System.out.println("Disconnected.");
    }
}
