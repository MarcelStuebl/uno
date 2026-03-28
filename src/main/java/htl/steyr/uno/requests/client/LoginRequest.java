package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record LoginRequest(String username, String password) implements Serializable {

    @Override
    public String toString() {
        return "LoginRequest{" + "username='" + username() + "'}";
    }
}