package htl.steyr.uno.server.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        System.out.println("UserNotFoundException: " + message);
    }
}
