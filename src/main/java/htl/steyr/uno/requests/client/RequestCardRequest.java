package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Player;

public class RequestCardRequest {

    private Player player;
    private int amount;

    public RequestCardRequest(Player player, int amount) {
        setPlayer(player);
        setAmount(amount);
    }

    @Override
    public String toString() {
        return "RequestCardRequest{" +
                "player_username=" + getPlayer().getUsername() +
                ", ammount=" + getAmount() +
                '}';
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
