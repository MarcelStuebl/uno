package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public record CardPlayedRequest(Card card, Integer drawPenaltyValue, Player player) implements Serializable {

    @Override
    public String toString() {
        return "CardPlayedRequest{" +
                "card='" + card +
                ", drawPenaltyValue=" + drawPenaltyValue +
                "'}";
    }
}