package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.*;
import htl.steyr.uno.GameTableClasses.exceptions.*;
import htl.steyr.uno.User;
import htl.steyr.uno.requests.client.*;
import htl.steyr.uno.requests.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLogic {

    private Lobby lobby;
    private ArrayList<Player> players = new ArrayList<>();
    private Integer currentPlayerIndex = 0;
    private boolean directionClockwise = true;
    private final Random random = new Random();


    public GameLogic(Lobby lobby) {
        setLobby(lobby);
    }
    
    
    /**
     * Game Setup Methods for initializing the game state and preparing the game for play.
     * <p>
     * This includes methods for creating the game, starting the game, and generating the initial hands for each player.
     * The GameLogic class is responsible for setting up the game state based on the players in the lobby and ensuring that each player receives their initial hand of cards.
     * The methods in this class should interact with the Lobby and Player classes to manage the game state and communicate with the clients as needed.
     */
    public void createGame(List<User> users) {
        players.clear();
        currentPlayerIndex = 0;
        directionClockwise = true;

        int playerIndex = 0;
        for (User user : users) {
            if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
                continue;
            }

            ArrayList<Card> hand = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                hand.add(generateCard());
            }

            players.add(new Player(user.getUsername(), false, hand, new ArrayList<>(), playerIndex));
            playerIndex++;
        }
    }


    /**
     * Handles the logic for when a player indicates that they are ready in the game lobby.
     * This method should update the player's ready status and check if all players are ready to start the game.
     *
     * @param msg The ReadyInGameTableRequest message containing information about the player who is ready.
     */
    public void readyInGameTable(ReadyInGameTableRequest msg) {
        for (Player player : players) {
            if (player.getUsername().equals(msg.getPlayer().getUsername())) {
                player.setReady(true);
                checkReadyInGameTable();
                break;
            }
        }
    }


    /**
     * Checks if all players in the game lobby are ready to start the game.
     * If all players are ready, this method will call the startGame() method to begin the game.
     * If any player is not ready, this method will simply return without starting the game.
     */
    private void checkReadyInGameTable() {
        for (Player player : players) {
            if (!player.isReady()) {
                return;
            }
        }
        startGame();
    }


    /**
     * Starts the game by sending the initial game state to all players in the lobby.
     * This method should send a PlayerGetResponse to each player with their initial hand of cards and any relevant game information.
     */
    public void startGame(){
        if (lobby == null || lobby.getConnections() == null || players.isEmpty()) {
            return;
        }

        for (Player player : players) {
            if (player == null || player.getUsername() == null) {
                continue;
            }
            for (ServerSocketConnection c : lobby.getConnections()) {
                if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                    continue;
                }
                if (c.getUser().getUsername().equals(player.getUsername())) {
                    for (Player enemy : players) {
                        if (!enemy.getUsername().equals(c.getUser().getUsername())) {
                            player.getEnemies().add(new Enemy(enemy));
                        }
                    }
                    PlayerGetResponse msg = new  PlayerGetResponse(player);
                    c.sendMessage(msg);
                    break;
                }
            }
        }
    }


    /**
     * Generates a random card for the game.
     * The card's value is randomly determined to be between 0 and 14, and the color is randomly selected from the four possible colors (yellow, green, blue, red).
     * The method ensures that the generated card is valid according to the game rules and returns a new Card object with the generated value and color.
     * @return A randomly generated Card object for use in the game.
     */
    private Card generateCard() {
        int value = random.nextInt(15); // 0–14
        String colour;

        if (value == 13 || value == 14) {
            // Spezialkarten sind immer schwarz
            colour = "black";
        } else {
            // Normale Farben
            String[] colors = {"yellow", "green", "blue", "red"};
            colour = colors[random.nextInt(colors.length)];
        }

        try {
            return new Card(value, colour);
        } catch (InvalidCardException e) {
            throw new RuntimeException("Error generating valid card", e);
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

        checkForWinner(player);

        CardPlayedResponse response = new CardPlayedResponse(card, player, currentPlayerIndex);
        for (ServerSocketConnection c : lobby.getConnections()) {
            c.sendMessage(response);
        }

    }

    private void updatePlayer(Player player) {
        if (player == null || player.getUsername() == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                continue;
            }
            if (c.getUser().getUsername().equals(player.getUsername())) {
                c.sendMessage(new PlayerGetResponse(player));
                break;
            }
        }
    }



    private void addCardsToPlayer(Player player, Card card) {
        if (player == null || player.getUsername() == null || card == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        player.addCardToHand(card);
        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                continue;
            }
            if (c.getUser().getUsername().equals(player.getUsername())) {
                c.sendMessage(new CardAddResponse(card));
                break;
            }
        }
    }



    public void requestCard(RequestCardRequest msg) {
        Player player = msg.getPlayer();
        int amount = msg.getAmount();

        for (int i = 0; i < amount; i++) {
            addCardsToPlayer(player, generateCard());
        }
    }


    private void checkForWinner(Player player) {
        if (player.getHand().isEmpty()) {
            player.setPassive(true);
            updatePlayer(player);
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

    public ArrayList<Enemy> getPlayersAsEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        for (Player player : players) {
            enemies.add(new Enemy(player));
        }
        return enemies;
    }
}
