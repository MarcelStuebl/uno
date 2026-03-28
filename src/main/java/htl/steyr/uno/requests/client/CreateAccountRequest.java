package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record CreateAccountRequest(String username, String lastName, String firstName, String email, String password, Integer code) implements Serializable {

    public CreateAccountRequest(String username, String lastName, String firstName, String email, String password) {
        this(username, lastName, firstName, email, password, null);
    }

    @Override
    public String toString() {
        return "CreateAccountRequest{" +
                "username='" + username() +
                "', lastName='" + lastName() +
                "', firstName='" + firstName() +
                "', email='" + email() +
                "', code='" + code() +
                "'}";
    }
}