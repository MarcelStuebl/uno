package htl.steyr.uno.server.serverconnection;

import htl.steyr.uno.GameTableClasses.Card;
import htl.steyr.uno.GameTableClasses.*;
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
    private final CardDeck cardDeck = new CardDeck();
    private Integer lastStackInfo = 0;
    private Card currentCard;


    GameLogic(Lobby lobby) {
        setLobby(lobby);
    }
    
    
    /**
     * Game Setup Methods for initializing the game state and preparing the game for play.
     * <p>
     * This includes methods for creating the game, starting the game, and generating the initial hands for each player.
     * The GameLogic class is responsible for setting up the game state based on the players in the lobby and ensuring that each player receives their initial hand of cards.
     * The methods in this class should interact with the Lobby and Player classes to manage the game state and communicate with the clients as needed.
     */
    void createGame(List<User> users) {
        players.clear();
        currentPlayerIndex = 0;
        directionClockwise = true;

        while (getCurrentCard() == null || getCurrentCard().getCardValue() >= 10) {
            setCurrentCard(getCardDeck().getCardFromStack());
        }
        getCardDeck().refill();

        int playerIndex = 0;
        for (User user : users) {
            if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
                continue;
            }

            ArrayList<Card> hand = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                hand.add(getCardDeck().getCardFromStack());
            }

            players.add(new Player(user.getUsername(), false, hand, new ArrayList<>(), playerIndex, user.getProfileImageData()));
            playerIndex++;
        }
    }


    /**
     * Handles the logic for when a player indicates that they are ready in the game lobby.
     * This method should update the player's ready status and check if all players are ready to start the game.
     *
     * @param msg The ReadyInGameTableRequest message containing information about the player who is ready.
     */
    void readyInGameTable(ReadyInGameTableRequest msg) {
        for (Player player : players) {
            if (player.getUsername().equals(msg.player().getUsername())) {
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
    private void startGame(){
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

                    GameTurnResponse gtr = new GameTurnResponse(null, getCurrentCard(), 0, getCurrentPlayer(), isDirectionClockwise());
                    c.sendMessage(gtr);
                    break;
                }
            }
        }
    }





    /**
     * Logic Methods for handling game mechanics.
     * <p>
     * This includes methods for adding cards to players, handling card plays, and updating the game state accordingly.
     * The GameLogic class is responsible for ensuring that the game rules are followed and that the game state is updated correctly based on player actions.
     * The methods in this class should interact with the Lobby and Player classes to manage the game state and communicate with the clients as needed.
     */




    void cardPlayed(CardPlayedRequest msg) {
        Card card = msg.card();
        Player player = msg.player();
        Integer drawPenaltyValue = msg.drawPenaltyValue();

        player.removeCardFromHand(card);
        getCardDeck().returnCardToDiscordPile(card);

        if (card.getCardValue() == 10) {
            // Skip Player
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        } else if (card.getCardValue() == 11) {
            // Change Direction
            directionClockwise = !directionClockwise;
        } else if (card.getCardValue() == 12) {
            drawPenaltyValue += 2;
        } else if (card.getCardValue() == 14) {
            drawPenaltyValue += 4;
        }

        currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();

        // überspringe passive Spieler
        while (players.get(currentPlayerIndex).isPassive()) {
            currentPlayerIndex = (currentPlayerIndex + (directionClockwise ? 1 : -1) + players.size()) % players.size();
        }

        checkForWinner(player);

        GameTurnResponse response = new GameTurnResponse(player.getPlayerIndex(), getCurrentCard(), drawPenaltyValue, currentPlayerIndex, directionClockwise);
        for (ServerSocketConnection c : lobby.getConnections()) {
            c.sendMessage(response);
        }

    }



    private void updateEnemy(Enemy enemy) {
        if (enemy == null || enemy.getUsername() == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                continue;
            }
            if (c.getUser().getUsername().equals(enemy.getUsername())) {
                c.sendMessage(new UpdateEnemyResponse(enemy));
                break;
            }
        }
    }



    private void addCardsToPlayer(Player player, Card card) {
        checkForEmptyStack();

        if (player == null || player.getUsername() == null || card == null || lobby == null || lobby.getConnections() == null) {
            return;
        }

        player.addCardToHand(card);
        for (ServerSocketConnection c : lobby.getConnections()) {
            if (c == null || c.getUser() == null || c.getUser().getUsername() == null) {
                continue;
            }
            if (c.getUser().getUsername().equals(player.getUsername())) {
                CardAddResponse msg = new CardAddResponse(card);
                c.sendMessage(msg);
                break;
            }
        }
    }



    void requestCard(RequestCardRequest msg) {
        Player player = msg.player();
        int amount = msg.amount();

        for (int i = 0; i < amount; i++) {
            Card card = getCardDeck().getCardFromStack();
            addCardsToPlayer(player, card);
        }
    }


    private void checkForWinner(Player player) {
        if (player.getHand().isEmpty()) {
            player.setPassive(true);
            updateEnemy(new Enemy(player));
        }
    }


    private void checkForEmptyStack() {
        if (getCardDeck().isStackEmpty() && lastStackInfo == 0) {
            getLobby().sendInfoToAll(new StackInfoResponse(1));
            lastStackInfo = 1;
        } else if (!getCardDeck().isStackEmpty() && lastStackInfo == 1) {
            getLobby().sendInfoToAll(new StackInfoResponse(0));
            lastStackInfo = 0;
        }
    }




    /**
     * End of Logic Methods.
     */



    ArrayList<Enemy> getPlayersAsEnemies() {
        ArrayList<Enemy> enemies = new ArrayList<>();
        for (Player player : players) {
            enemies.add(new Enemy(player));
        }
        return enemies;
    }

    Lobby getLobby() {
        return lobby;
    }
    void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    ArrayList<Player> getPlayers() {
        return players;
    }
    Player getPlayer(int i) {
        return getPlayers().get(i);
    }
    void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    Integer getCurrentPlayer() {
        return currentPlayerIndex;
    }
    void setCurrentPlayer(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    boolean isDirectionClockwise() {
        return directionClockwise;
    }
    void setDirectionClockwise(boolean directionClockwise) {
        this.directionClockwise = directionClockwise;
    }

    private CardDeck getCardDeck() {
        return cardDeck;
    }

    private Card getCurrentCard() {
        return currentCard;
    }
    private void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }
}
