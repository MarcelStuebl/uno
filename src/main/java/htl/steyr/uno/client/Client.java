package htl.steyr.uno.client;

import htl.steyr.uno.client.requests.CreateAccountRequest;
import htl.steyr.uno.client.requests.LoginRequest;

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
                System.out.println("Enter username:");
                String username = console.nextLine();
                if (username.equalsIgnoreCase("/quit")) break;

                System.out.println("Enter password:");
                String password = console.nextLine();
                if (password.equalsIgnoreCase("/quit")) break;

                LoginRequest msg = new LoginRequest(username, password);
                conn.sendMessage(msg);
                String input = console.nextLine();
                break;

//                String username = "testuser";
//                String password = "123";
//                String firstName = "Test";
//                String lastName = "User";
//                CreateAccountRequest msg = new CreateAccountRequest(username, lastName, firstName, password);
//                conn.sendMessage(msg);
//                String input = console.nextLine();
//                break;
            }
        }

        conn.close();
        System.out.println("Disconnected.");
    }
}
