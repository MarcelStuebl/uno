package htl.steyr.uno.requests.server;

import java.io.Serializable;

public record CheckIfUserAlreadyExistsResponse(String username, String email, boolean userAlreadyExists, boolean emailAlreadyExists) implements Serializable {

    @Override
    public String toString() {
        return "CheckIfUserAlreadyExistsRequest{" +
                "username='" + username() +
                '}';
    }
}