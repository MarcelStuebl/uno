package htl.steyr.uno.server.serverconnection;

public class GameLogic {

    private Lobby lobby;


    public GameLogic(Lobby lobby) {
        setLobby(lobby);
    }

    public void startGame() {



    }


    public Lobby getLobby() {
        return lobby;
    }
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }


}
