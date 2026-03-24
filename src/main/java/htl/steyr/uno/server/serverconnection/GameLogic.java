package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.Player;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidHandException;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidPlayerException;
import htl.steyr.uno.requests.server.CardAddResponse;
import htl.steyr.uno.requests.server.CardRemoveResponse;

import java.util.ArrayList;

public class GameLogic {

    private Lobby lobby;
    private ArrayList<Player> players = new ArrayList<>();


    public GameLogic(Lobby lobby) {
        setLobby(lobby);
    }



    public void startGame() throws InvalidPlayerException, InvalidHandException {
        for (ServerSocketConnection c : lobby.getConnections()) {
            Player player = new Player(c.getUser().getUsername(), false, new ArrayList<>(), new ArrayList<>());
            for (int i = 0; i < 7; i++) {
                addCardsToPlayer(player, generateCard());
            }
            players.add(player);
        }


    }

    private Card generateCard() {
        int value = (int) (Math.random() * 15);
        String colour;
        switch ((int) (Math.random() * 4)) {
            case 0 -> colour = "yellow";
            case 1 -> colour = "green";
            case 2 -> colour = "blue";
            case 3 -> colour = "red";
            default -> throw new IllegalStateException("Unexpected value: " + (int) (Math.random() * 4));
        }
        try {
            return new Card(value, colour);
        } catch (InvalidCardException e) {
            throw new RuntimeException(e);
        }
    }


    private void addCardsToPlayer(Player player, Card card) {
        player.addCardToHand(card);
        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c.getUser().getUsername().equals(player.getUsername())) {
                c.sendMessage(new CardAddResponse(card));
            }
        }
    }

    private void removeCardsFromPlayer(Player player, Card card) {
        player.removeCardFromHand(card);
        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c.getUser().getUsername().equals(player.getUsername())) {
                c.sendMessage(new CardRemoveResponse(card));
            }
        }
    }

    public Lobby getLobby() {
        return lobby;
    }
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
    public Player getPlayer(int i) {
        return getPlayers().get(i);
    }
    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }
}
