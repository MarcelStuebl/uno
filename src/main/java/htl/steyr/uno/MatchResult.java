package htl.steyr.uno;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MatchResult implements Serializable {
    private final int matchId;
    private final ArrayList<MatchPlayer> players;
    private final Timestamp timestamp;

    /**
     * @param matchId   ID des Matches
     * @param players   die Spieler des Matches, sortiert nach Platzierung (Index 0 = 1. Platz)
     * @param timestamp Zeitpunkt des Matches
     */
    @JsonCreator
    public MatchResult(@JsonProperty("matchId") int matchId,
                       @JsonProperty("players") ArrayList<MatchPlayer> players,
                       @JsonProperty("timestamp") Timestamp timestamp) {
        this.matchId = matchId;
        this.players = players;
        this.timestamp = timestamp;
    }

    public int getMatchId() {
        return matchId;
    }

    public ArrayList<MatchPlayer> getPlayers() {
        return players;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

}
