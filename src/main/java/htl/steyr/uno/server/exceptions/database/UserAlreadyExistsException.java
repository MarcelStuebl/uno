package htl.steyr.uno.server.exceptions.database;

import htl.steyr.uno.server.exceptions.user.UserException;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String message) {
        super("UserAlreadyExistsException: " + "User '" + message + "' already exists.");
    }
}




