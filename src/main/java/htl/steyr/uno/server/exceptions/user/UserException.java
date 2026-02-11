package htl.steyr.uno.server.exceptions.user;

public class UserException extends RuntimeException {

    public UserException(){
        System.out.println("UserException: An error occurred with the user operation.");
    }

    public UserException(String message) {
        super(message);
    }
}
