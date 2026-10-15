package htl.steyr.uno;

import java.io.Serializable;
import java.sql.Timestamp;

public class MatchResult implements Serializable {
    private final int matchId;
    private final int[] playerIds;
    private final Timestamp timestamp;

    public MatchResult(int matchId, int[] playerIds) {
        this.matchId = matchId;
        this.playerIds = playerIds;
        this.timestamp = null;
    }

    public MatchResult(int matchId, int[] playerIds, Timestamp timestamp) {
        this.matchId = matchId;
        this.playerIds = playerIds;
        this.timestamp = timestamp;
    }

    public int getMatchId() {
        return matchId;
    }

    public int[] getPlayerIds() {
        return playerIds;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

}
