package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public record SetProfileImageRequest(User user, byte[] imageData) implements Serializable {

    @Override
    public String toString() {
        return "SetProfileImageRequest{" +
                "username='" + user().getUsername() +
                "'}";
    }
}