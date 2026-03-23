package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Card;

import java.io.Serializable;

public class RemoveCardResponse implements Serializable {

    private Card card;

    public RemoveCardResponse(Card card) {
        setCard(card);
    }

    @Override
    public String toString(){
        return "RemoveCardResponse{" +
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
