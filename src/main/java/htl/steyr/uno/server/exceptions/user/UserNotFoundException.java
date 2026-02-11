package htl.steyr.uno.server.exceptions.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super("UserNotFoundException: " + message);
    }
}




