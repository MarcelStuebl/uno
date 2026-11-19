package htl.steyr.uno.requests.server;

import java.io.Serializable;
import java.util.ArrayList;

public record GetAllLobbiesResponse(ArrayList<LobbyInfoResponse> lobbies) implements Serializable {
    @Override
    public String toString() {
        return "GetAllLobbiesResponse{" +
                "lobbies=" + lobbies +
                '}';
    }
}
