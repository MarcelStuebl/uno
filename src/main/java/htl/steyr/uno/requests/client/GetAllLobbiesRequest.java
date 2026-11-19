package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record GetAllLobbiesRequest() implements Serializable {
    @Override
    public String toString() {
        return "GetAllLobbiesRequest{}";
    }
}
