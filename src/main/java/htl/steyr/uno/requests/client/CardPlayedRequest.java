package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public class CardPlayedRequest implements Serializable {

    private Card card;
    private Player player;

    public CardPlayedRequest(Card card, Player player) {
        setCard(card);
        setPlayer(player);
    }

    @Override
    public String toString(){
        return "CardPlayedRequest{" +
                "card='" + card +
                "'}";
    }

    public void setCard(Card card) {
        this.card = card;
    }
    public Card getCard() {
        return card;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    public Player getPlayer() {
        return player;
    }


}
