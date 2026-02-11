package htl.steyr.uno.server.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        System.out.println("InvalidPasswordException: Invalid password provided.");
    }
}
