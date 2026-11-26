package htl.steyr.uno.GameTableClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import htl.steyr.uno.User;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class Player implements Serializable {
    private String username;
    private boolean isCurrentTurn;
    private final ArrayList<Card> hand = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private Integer playerIndex;
    private boolean isReady = false;
    private boolean isPassive;
    @EqualsAndHashCode.Exclude @ToString.Exclude private byte[] imageBytes;

    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies,
                  Integer playerIndex, boolean isPassive, byte[] imageBytes) {
        this(username, isCurrentTurn, hand, enemies, playerIndex, isPassive, imageBytes, false);
    }

    @JsonCreator
    public Player(@JsonProperty("username") String username,
                  @JsonProperty("currentTurn") boolean isCurrentTurn,
                  @JsonProperty("hand") ArrayList<Card> hand,
                  @JsonProperty("enemies") ArrayList<Enemy> enemies,
                  @JsonProperty("playerIndex") Integer playerIndex,
                  @JsonProperty("passive") boolean isPassive,
                  @JsonProperty("imageBytes") byte[] imageBytes,
                  @JsonProperty("ready") boolean isReady) {
        setUsername(username);
        setCurrentTurn(isCurrentTurn);
        getHand().addAll(hand);
        setEnemies(enemies);
        setPlayerIndex(playerIndex);
        setPassive(isPassive);
        sortHand();
        setImageBytes(imageBytes);
        setReady(isReady);
    }

    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies, Integer playerIndex, byte[] imageBytes) {
        this(username, isCurrentTurn, hand, enemies, playerIndex, false, imageBytes);
    }

    public Player(User user) {
        this(user.getUsername(), false, new ArrayList<>(), new ArrayList<>(), -1, false, user.getProfileImageData());
    }


    private void sortHand() {
        this.hand.sort((c1, c2) -> {

            // Farben-Reihenfolge
            int colorCompare = getColorOrder(c1.getCardColour())
                    - getColorOrder(c2.getCardColour());

            if (colorCompare != 0) return colorCompare;

            // Dann einfach nach Wert sortieren
            return c1.getCardValue() - c2.getCardValue();
        });
    }

    private int getColorOrder(String color) {
        return switch (color) {
            case "red" -> 0;
            case "green" -> 1;
            case "yellow" -> 2;
            case "blue" -> 3;
            case "black" -> 4;
            default -> 5;
        };
    }


    public void setEnemies(ArrayList<Enemy> enemies) {
        this.enemies.clear();
        this.enemies.addAll(enemies);
    }

    public Enemy getEnemyByUsername(String username) {
        for (Enemy e : this.enemies) {
            if (e.getUsername().equals(username)) {
                return e;
            }
        }
        return null;
    }

    public void addCardToHand(Card card) {
        this.hand.add(card);
        sortHand();
    }

    public void removeCardFromHand(Card card) {
        for (int i = 0; i < this.hand.size(); i++) {
            Card c = this.hand.get(i);
            // Vergleiche nach cardId um sicherzustellen, dass nur die GENAUE Karte entfernt wird
            // Nicht die erste Karte mit gleichem CardValue und CardColour
            if (c.getCardId().equals(card.getCardId())) {
                this.hand.remove(i);
                return;
            }
        }
    }

}