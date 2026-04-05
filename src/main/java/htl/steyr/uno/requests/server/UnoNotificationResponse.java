package htl.steyr.uno.requests.server;

import java.io.Serializable;

public record UnoNotificationResponse(String username, boolean didSayUno) implements Serializable {

    @Override
    public String toString() {
        return "UnoNotificationResponse{" +
                "username='" + username + '\'' +
                ", didSayUno=" + didSayUno +
                '}';
    }
}

