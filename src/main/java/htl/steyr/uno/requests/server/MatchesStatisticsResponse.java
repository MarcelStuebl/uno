package htl.steyr.uno.requests.server;

import htl.steyr.uno.MatchResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @param matches       die Matches des Benutzers, neuestes zuerst
 * @param profileImages Profilbilder (Thumbnails) aller vorkommenden Spieler, nach User-ID;
 *                      Spieler ohne Profilbild fehlen in der Map
 */
public record MatchesStatisticsResponse(ArrayList<MatchResult> matches,
                                        HashMap<Integer, byte[]> profileImages) implements Serializable {

    @Override
    public String toString() {
        return "MatchesStatisticsResponse{" +
                "matches=" + matches +
                ", profileImages=" + (profileImages != null ? profileImages.size() : 0) +
                '}';
    }
}
