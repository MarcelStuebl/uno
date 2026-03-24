package htl.steyr.uno.server.exceptions.database;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String message) {
        super("UserAlreadyExistsException: " + "User '" + message + "' already exists.");
    }
}




