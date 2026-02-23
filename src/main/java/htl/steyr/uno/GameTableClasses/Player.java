package htl.steyr.uno.GameTableClasses;

import java.util.ArrayList;

public class Player {
    private final ArrayList<Card> hand = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();




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


    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }
    public Enemy getEnemy(int index) {
        return this.enemies.get(index);
    }


}
