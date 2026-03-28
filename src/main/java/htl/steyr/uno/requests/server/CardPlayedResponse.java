package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.Enemy;
import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public record CardPlayedResponse(Card card, Enemy enemy, Integer nextPlayerIndex) implements Serializable {

    public CardPlayedResponse(Card card, Player player, Integer nextPlayerIndex) {
        this(card, new Enemy(player), nextPlayerIndex);
    }

    @Override
    public String toString() {
        return "CardPlayedResponse{" +
                "card=" + card() +
                ", enemy_username=" + enemy().getUsername() +
                ", nextPlayerIndex=" + nextPlayerIndex() +
                '}';
    }
}