package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Card;

import java.io.Serializable;

public record CardAddResponse(Card card) implements Serializable {

    @Override
    public String toString() {
        return "AddCardResponse{" +
                "card='" + card() +
                "'}";
    }
}