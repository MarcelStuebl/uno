package htl.steyr.uno.requests.server;

import htl.steyr.uno.MatchResult;

import java.io.Serializable;
import java.util.ArrayList;

public record MatchesStatisticsResponse(ArrayList<MatchResult> matches) implements Serializable {

    @Override
    public String toString() {
        return "MatchesStatisticsResponse{" +
                "matches=" + matches +
                '}';
    }
}
