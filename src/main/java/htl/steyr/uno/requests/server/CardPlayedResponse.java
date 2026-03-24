package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.Enemy;
import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public class CardPlayedResponse implements Serializable {

    private Card card;
    private Enemy enemy;
    private Integer nextPlayerIndex;

    public CardPlayedResponse(Card card, Enemy enemy, Integer nextPlayerIndex) {
        setCard(card);
        setEnemy(enemy);
        setNextPlayerIndex(nextPlayerIndex);
    }

    public CardPlayedResponse(Card card, Player player, Integer nextPlayerIndex) {
        setCard(card);
        setEnemy(new Enemy(player));
        setNextPlayerIndex(nextPlayerIndex);
    }


    @Override
    public String toString() {
        return "CardPlayedResponse{" +
                "card=" + getCard() +
                ", enemy_username=" + getEnemy().getUsername() +
                ", nextPlayerIndex=" + getNextPlayerIndex() +
                '}';
    }



    public Card getCard() {
        return card;
    }
    public void setCard(Card card) {
        this.card = card;
    }

    public Enemy getEnemy() {
        return enemy;
    }
    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
    }

    public Integer getNextPlayerIndex() {
        return nextPlayerIndex;
    }
    public void setNextPlayerIndex(Integer nextPlayerIndex) {
        this.nextPlayerIndex = nextPlayerIndex;
    }

}
