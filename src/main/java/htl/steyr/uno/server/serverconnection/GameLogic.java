package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.Player;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidHandException;
import htl.steyr.uno.GameTableClasses.exceptions.InvalidPlayerException;
import htl.steyr.uno.requests.client.CardPlayedRequest;
import htl.steyr.uno.requests.server.CardAddResponse;
import htl.steyr.uno.requests.server.CardPlayedResponse;
import htl.steyr.uno.requests.server.PlayerGetResponse;

import java.util.ArrayList;

public class GameLogic {

    private Lobby lobby;
    private ArrayList<Player> players = new ArrayList<>();
    private Integer currentPlayerIndex = 0;
    private boolean directionClockwise = true;


    public GameLogic(Lobby lobby) {
        setLobby(lobby);
    }


    public void startGame() throws InvalidPlayerException, InvalidHandException {
        int playerIndex = 0;
        for (ServerSocketConnection c : lobby.getConnections()) {
            Player player = new Player(c.getUser().getUsername(), false, new ArrayList<>(), new ArrayList<>(), playerIndex);
            for (int i = 0; i < 7; i++) {
                player.addCardToHand(generateCard());
            }
            c.sendMessage(new PlayerGetResponse(player));
            players.add(player);
            playerIndex++;
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




    /**
     * Logic Methods for handling game mechanics.
     * <p>
     * This includes methods for adding cards to players, handling card plays, and updating the game state accordingly.
     * The GameLogic class is responsible for ensuring that the game rules are followed and that the game state is updated correctly based on player actions.
     * The methods in this class should interact with the Lobby and Player classes to manage the game state and communicate with the clients as needed.
     */




    public void cardPlayed(CardPlayedRequest msg) {
        Card card = msg.getCard();
        Player player = msg.getPlayer();

        player.removeCardFromHand(card);

        if (card.getCardValue() == 10) {
            // Skip Player
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        } else if (card.getCardValue() == 11) {
            // Change Direction
            directionClockwise = !directionClockwise;
        }

        currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();

        CardPlayedResponse response = new CardPlayedResponse(card, player, currentPlayerIndex);
        for (ServerSocketConnection c : lobby.getConnections()) {
            c.sendMessage(response);
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









    /**
     * End of Logic Methods.
     */





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

    public Integer getCurrentPlayer() {
        return currentPlayerIndex;
    }
    public void setCurrentPlayer(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public boolean isDirectionClockwise() {
        return directionClockwise;
    }
    public void setDirectionClockwise(boolean directionClockwise) {
        this.directionClockwise = directionClockwise;
    }
}
