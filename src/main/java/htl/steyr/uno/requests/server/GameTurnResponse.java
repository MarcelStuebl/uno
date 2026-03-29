package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Card;

import java.io.Serializable;

public record GameTurnResponse(Integer enemyIndex, Card card, Integer nextPlayerIndex, boolean directionClockwise) implements Serializable {

    @Override
    public String toString() {
        return "GameTurnResponse{" +
                "enemyIndex=" + enemyIndex +
                ", card=" + card +
                ", nextPlayerIndex=" + nextPlayerIndex +
                ", directionClockwise=" + directionClockwise +
                '}';
    }

}
