package htl.steyr.uno.requests.server;

import htl.steyr.uno.GameTableClasses.Player;

import java.io.Serializable;

public class PlayerGetResponse implements Serializable {

    private Player player;

    public PlayerGetResponse(Player player) {
        setPlayer(player);
    }

    @Override
    public String toString() {
        return "PlayerGetResponse{" +
                "player_username='" + getPlayer().getUsername() +
                "'}";
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }


}
