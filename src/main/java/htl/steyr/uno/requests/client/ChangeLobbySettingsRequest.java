package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record ChangeLobbySettingsRequest(Integer lobbyId, Integer mode, Integer type) implements Serializable {

    @Override
    public String toString() {
        return "ChangeLobbySettingsRequest{" +
                "lobbyId=" + lobbyId +
                ", mode=" + mode +
                ", type=" + type +
                '}';
    }
}
