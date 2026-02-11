package htl.steyr.uno.server.exceptions.user;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String message) {
        super("UserAlreadyExistsException: " + "User '" + message + "' already exists.");
    }
}




