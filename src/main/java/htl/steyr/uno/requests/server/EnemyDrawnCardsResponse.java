package htl.steyr.uno.requests.server;

import java.io.Serializable;

public record EnemyDrawnCardsResponse(Integer playerIndex, Integer cardsDrawn) implements Serializable {

    @Override
    public String toString() {
        return "EnemyDrawnCardsResponse{" +
                "playerIndex=" + playerIndex +
                ", cardsDrawn=" + cardsDrawn +
                '}';
    }
}

