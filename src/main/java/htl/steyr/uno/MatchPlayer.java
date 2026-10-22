package htl.steyr.uno;

import java.io.Serializable;

/**
 * Ein Spieler eines vergangenen Matches mit seiner Platzierung.
 *
 * @param userId    ID des Benutzers
 * @param username  Benutzername (null, falls der Benutzer nicht mehr existiert)
 * @param placement Platzierung im Match (1 = Sieger)
 */
public record MatchPlayer(int userId, String username, int placement) implements Serializable {
}
