package htl.steyr.uno.requests.client;

import java.io.Serial;
import java.io.Serializable;

public record UserReadyRequest(boolean ready, String username) implements Serializable {

    @Override
    public String toString() {
        return "UserReadyRequest{" +
                "ready=" + ready +
                ", username='" + username + '\'' +
                '}';
    }
}
