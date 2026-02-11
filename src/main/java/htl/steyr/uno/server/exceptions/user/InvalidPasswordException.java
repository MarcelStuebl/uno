package htl.steyr.uno.server.exceptions.user;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("InvalidPasswordException: Invalid password provided.");
    }
}




