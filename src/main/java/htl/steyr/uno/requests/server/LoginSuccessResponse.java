package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public record LoginSuccessResponse(User user) implements Serializable {

    @Override
    public String toString() {
        return "LoginSuccessResponse{" +
                "username='" + user().getUsername() +
                "'}";
    }
}