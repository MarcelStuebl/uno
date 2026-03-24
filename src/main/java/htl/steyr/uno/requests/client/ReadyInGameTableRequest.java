package htl.steyr.uno.requests.client;

import htl.steyr.uno.GameTableClasses.Player;

public class ReadyInGameTableRequest {

    private Player player;

    public ReadyInGameTableRequest(Player player) {
        setPlayer(player);
    }

    @Override
    public String toString() {
        return "ReadyInGameTableRequest{" +
                "player=" + getPlayer().getUsername() +
                '}';
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

}
