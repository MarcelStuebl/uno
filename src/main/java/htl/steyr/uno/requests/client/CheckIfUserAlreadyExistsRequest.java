package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record CheckIfUserAlreadyExistsRequest(String username, String email) implements Serializable {

    @Override
    public String toString() {
        return "CheckIfUserAlreadyExistsRequest{" +
                "username='" + username() +
                "', email='" + email() +
                "'}";
    }
}