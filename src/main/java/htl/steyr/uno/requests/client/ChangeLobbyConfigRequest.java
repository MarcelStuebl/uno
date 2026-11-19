package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record ChangeLobbyConfigRequest(Integer lobbyMode, Integer LobbyType) implements Serializable {
    @Override
    public String toString() {
        return "ChangeLobbyConfigRequest{" +
                "lobbyMode=" + lobbyMode +
                ", LobbyType=" + LobbyType +
                '}';
    }
}
