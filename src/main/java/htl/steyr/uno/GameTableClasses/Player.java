package htl.steyr.uno.GameTableClasses;

import java.util.ArrayList;

public class Player {
    private final String username;
    private final boolean isCurrentTurn;
    private final ArrayList<Card> hand = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();

    public Player(String username, boolean isCurrentTurn, ArrayList<Card> hand, ArrayList<Enemy> enemies) {
        this.username = username;
        this.isCurrentTurn = isCurrentTurn;
        this.hand.addAll(hand);
        this.enemies.addAll(enemies);
        sortHand();
    }


    public String getUsername() {
        return this.username;
    }

    public ArrayList<Card> getPlayerHand() {
        return this.hand;
    }

    public void setPlayerHand(ArrayList<Card> arr) {
        this.hand.clear();
        this.hand.addAll(arr);
    }
    public void addCardToHand(Card card) {
        this.hand.add(card);
    }
    public void removeCardFromHand(Card card) {
        this.hand.remove(card);
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }

    public Enemy getEnemy(int index) {
        return this.enemies.get(index);
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
        switch (color) {
            case "red": return 0;
            case "green": return 1;
            case "yellow": return 2;
            case "blue": return 3;
            case "black": return 4;
            default: return 5;
        }
    }

    public void testPrintHand() {
        System.out.println("Hand von " + username + ":");

        for (Card card : hand) {
            System.out.println(
                    card.getCardColour() + " " + card.getCardValue()
            );
        }

        System.out.println("----------------------");
    }

}
