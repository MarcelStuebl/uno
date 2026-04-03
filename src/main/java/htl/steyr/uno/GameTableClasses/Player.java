package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.User;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    private String username;
    private boolean isCurrentTurn;
    private final ArrayList<Card> hand = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private Integer playerIndex;
    private boolean isReady = false;
    private boolean isPassive;
    private byte[] imageBytes;

    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies, Integer playerIndex, boolean isPassive, byte[] imageBytes) {
        setUsername(username);
        setCurrentTurn(isCurrentTurn);
        getHand().addAll(hand);
        setEnemies(enemies);
        setPlayerIndex(playerIndex);
        setPassive(isPassive);
        sortHand();
        setImageBytes(imageBytes);
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


    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", isCurrentTurn=" + isCurrentTurn +
                ", hand=" + hand +
                ", enemies=" + enemies +
                ", playerIndex=" + playerIndex +
                ", isReady=" + isReady +
                ", isPassive=" + isPassive +
                '}';
    }


    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(Integer playerIndex) {
        this.playerIndex = playerIndex;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isPassive() {
        return isPassive;
    }

    public void setPassive(boolean passive) {
        isPassive = passive;
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }

    public void setCurrentTurn(boolean currentTurn) {
        this.isCurrentTurn = currentTurn;
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
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

    public ArrayList<Card> getHand() {
        return this.hand;
    }

    public void addCardToHand(Card card) {
        this.hand.add(card);
        sortHand();
    }

    public void removeCardFromHand(Card card) {
        for (int i = 0; i < this.hand.size(); i++) {
            Card c = this.hand.get(i);
            if (c.getCardValue() == card.getCardValue() && c.getCardColour().equals(card.getCardColour())) {
                this.hand.remove(i);
                return;
            }
        }
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

}