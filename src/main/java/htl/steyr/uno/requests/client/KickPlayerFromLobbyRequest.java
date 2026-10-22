package htl.steyr.uno.requests.client;


import java.io.Serializable;

public record KickPlayerFromLobbyRequest(String username) implements Serializable {

    @Override
    public String toString() {
        return "KickPlayerFromLobbyRequest{" +
                "username='" + username() + '\'' +
                '}';
    }

}
