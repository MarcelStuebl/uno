package htl.steyr.uno.server.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        System.out.println("UserAlreadyExistsException: " + message);
    }
}
