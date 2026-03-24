package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Card;

import java.io.Serializable;

public class CardAddResponse implements Serializable {

    private Card card;

    public CardAddResponse(Card card) {
        setCard(card);
    }

    @Override
    public String toString(){
        return "AddCardResponse{" +
                "card='" + card +
                "'}";
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }


}
