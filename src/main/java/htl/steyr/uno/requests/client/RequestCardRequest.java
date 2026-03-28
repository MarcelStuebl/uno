package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Player;

public record RequestCardRequest(Player player, int amount) {

    @Override
    public String toString() {
        return "RequestCardRequest{" +
                "player_username=" + player().getUsername() +
                ", ammount=" + amount() +
                '}';
    }
}