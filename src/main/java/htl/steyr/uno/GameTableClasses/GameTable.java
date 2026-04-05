package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.GameOverResponse;
import htl.steyr.uno.requests.server.GameTurnResponse;
import htl.steyr.uno.requests.server.StartGameResponse;
import htl.steyr.uno.requests.server.UnoNotificationResponse;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GameTable implements Initializable {

    private final Client client;
    @FXML private StackPane root;
    CardStack cardStack = new CardStack(this);
    private final StartGameResponse startGameResponse;
    private GameLogic gameLogic;
    private Player player;
    private HBox handBox;
    private GameTurnResponse gameTurnResponse;
    private Button withdrawalButton;
    private boolean gameOverOverlayShown = false;
    @FXML private Label yourTurnLabel;

    // Variables used for the player hand UI.
    private VBox handVBox; // VBox holding multiple rows of cards
    private int maxCardsPerRow = 30; // maximum cards in one row

    @FXML private StackPane enemy1;
    @FXML private StackPane enemy2;
    @FXML private StackPane enemy3;

    private final ArrayList<EnemyDisplayController> enemyControllers = new ArrayList<>();

    private Button sayUnoButton; // Button to say "UNO"
    private boolean unoTimeActive = false; // Indicates if the UNO countdown is active
    private Timeline unoCountdownTimer; // Timer for the UNO countdown

    public GameTable(Client client, StartGameResponse msg) {
        this.client = client;
        this.startGameResponse = msg;
        this.player = new Player(getClient().getConn().getUser());
    }

    private void updatePlayerFromStartGameResponse() {
        for (Enemy enemy : startGameResponse.enemies()) {
            if (!Objects.equals(enemy.getUsername(), player.getUsername())) {
                player.getEnemies().add(enemy);
                break;
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updatePlayerFromStartGameResponse();
        gameLogic = new GameLogic(this);
        gameLogic.sendReadyToStart();

        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("UNO - Game Table");
            stage.setMaximized(true);
            stage.setResizable(true);

            setupCentralStack();
            addCloseButton(root, stage);
            createWithdrawalStack(root);
            createSayUnoButton(root);

            if (yourTurnLabel != null) {
                StackPane.setAlignment(yourTurnLabel, Pos.TOP_CENTER);
                StackPane.setMargin(yourTurnLabel, new Insets(90, 0, 0, 0));
                yourTurnLabel.toFront();
            }

            if (yourTurnLabel != null && getClient().getConn() != null && getClient().getConn().getUser() != null) {
                String myUsername = getClient().getConn().getUser().getUsername();
                if (startGameResponse.enemies().getFirst().getUsername().equals(myUsername)) {
                    yourTurnLabel.setText("Your Turn!");
                } else {
                    yourTurnLabel.setText("");
                }
            }

            if (sayUnoButton != null) {
                sayUnoButton.toFront();
            }
        });
    }

    private void setupCentralStack() {
        StackPane.setAlignment(cardStack.getVisual(), javafx.geometry.Pos.CENTER);
        cardStack.getVisual().setPickOnBounds(false);
        root.getChildren().add(cardStack.getVisual());
    }

    /**
     * Updates the enemy displays based on the current enemy list in the player object.
     */
    public void setEnemies() {
        List<Enemy> enemies = player.getEnemies();
        int myIndex = player.getPlayerIndex();
        int totalPlayers = enemies.size() + 1;

        enemyControllers.clear();

        List<Enemy> sorted = new ArrayList<>(enemies);
        sorted.sort((a, b) -> {
            int ai = (a.getPlayerIndex() - myIndex + totalPlayers) % totalPlayers;
            int bi = (b.getPlayerIndex() - myIndex + totalPlayers) % totalPlayers;
            return Integer.compare(ai, bi);
        });

        StackPane[] slotOrder = { enemy3, enemy1, enemy2 };

        int[] slotIndices = switch (sorted.size()) {
            case 1 -> new int[]{1};
            case 2 -> new int[]{0, 2};
            case 3 -> new int[]{0, 1, 2};
            default -> new int[]{};
        };

        for (int i = 0; i < slotIndices.length && i < sorted.size(); i++) {
            Enemy enemy = sorted.get(i);
            StackPane slot = slotOrder[slotIndices[i]];
            EnemyDisplayController ctrl = addPlayer(slot, enemy.getUsername(), enemy.getImageBytes(), enemy.getHandSize(), enemy.isPassive());
            if (ctrl != null) {
                enemyControllers.add(ctrl);
            }
        }

        Integer activePlayerIndex = gameTurnResponse != null ? gameTurnResponse.nextPlayerIndex() : null;
        refreshEnemyTurnHighlight(activePlayerIndex);
    }

    /**
     * Updates the highlight state of enemy displays to indicate whose turn it is.
     */
    public void refreshEnemyTurnHighlight(Integer activePlayerIndex) {
        if (player == null) {
            return;
        }

        for (EnemyDisplayController ctrl : enemyControllers) {
            if (ctrl == null) {
                continue;
            }

            boolean isActiveTurn = false;
            if (activePlayerIndex != null) {
                for (Enemy enemy : player.getEnemies()) {
                    if (enemy != null
                            && enemy.getUsername() != null
                            && enemy.getUsername().equals(ctrl.getUsername())
                            && activePlayerIndex.equals(enemy.getPlayerIndex())) {
                        isActiveTurn = true;
                        break;
                    }
                }
            }

            ctrl.setTurnActive(isActiveTurn);
        }
    }

    /**
     * Adds an enemy display to the specified slot.
     */
    private EnemyDisplayController addPlayer(StackPane slot, String name, byte[] imageData, int cardCount, boolean passive) {
        Image profileImage;
        if (imageData != null && imageData.length > 0) {
            profileImage = new Image(new ByteArrayInputStream(imageData), 50, 50, true, true);
        } else {
            profileImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")),
                    50, 50, true, true
            );
        }

        URL url = GameTable.class.getResource("/htl/steyr/uno/enemy.fxml");
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Node panel = loader.load();
            EnemyDisplayController ctrl = loader.getController();
            ctrl.setUsername(name);
            ctrl.setCardCount(cardCount);
            ctrl.setProfileImage(profileImage);
            ctrl.setPassive(passive);
            slot.getChildren().setAll(panel);
            return ctrl;
        } catch (IOException e) {
            System.err.println("Fehler beim Laden von enemy.fxml");
            return null;
        }
    }

    private void onSceneClose() {
        client.setGameTable(null);
        client.shutdown();
    }

    /**
     * Creates the player's hand card display at the bottom of the scene.
     */
    public void open() {
        handVBox = new VBox();
        handVBox.setAlignment(Pos.BOTTOM_CENTER);
        handVBox.setSpacing(-200);
        handVBox.setMouseTransparent(false);

        updatePlayerHandUI();

        StackPane.setAlignment(handVBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(handVBox, new Insets(40));
        root.getChildren().add(handVBox);

        if (withdrawalButton != null) {
            withdrawalButton.toFront();
        }
        if (sayUnoButton != null) {
            sayUnoButton.toFront();
        }
    }

    /**
     * Calculates the horizontal spacing between cards based on the number of cards in the row.
     */
    private double calculateSpacing(int cardCount) {
        double minSpacing = -140;
        double maxSpacing = -50;

        if (cardCount <= 5) return maxSpacing;
        if (cardCount >= maxCardsPerRow) return minSpacing;

        return maxSpacing - ((cardCount - 5) * (Math.abs(maxSpacing - minSpacing) / (maxCardsPerRow - 5)));
    }

    /**
     * Updates the player's hand card display.
     */
    public void updatePlayerHandUI() {
        if (handVBox == null) return;

        handVBox.getChildren().clear();

        List<Card> cards = new ArrayList<>(player.getHand());
        int index = 0;
        int rowNumber = 0;

        while (index < cards.size()) {
            HBox row = new HBox();
            row.setAlignment(Pos.BOTTOM_CENTER);

            int remaining = cards.size() - index;
            int cardsInThisRow = Math.min(remaining, maxCardsPerRow);

            double spacing = calculateSpacing(cardsInThisRow);
            row.setSpacing(spacing);

            for (int i = 0; i < cardsInThisRow; i++) {
                Button cardButton = createCardButton(cards.get(index));
                cardButton.setTranslateY(40 * rowNumber);
                row.getChildren().add(cardButton);
                index++;
            }

            row.setTranslateY(10 * rowNumber);
            rowNumber++;

            handVBox.getChildren().add(row);
        }

        if (withdrawalButton != null) {
            withdrawalButton.toFront();
        }
        if (sayUnoButton != null) {
            sayUnoButton.toFront();
        }
    }

    /**
     * Creates a card button for the player's hand.
     */
    private Button createCardButton(Card c) {
        String path = "/htl/steyr/uno/Uno_Cards/" + c.getCardColour() + "/" + c.getCardColour() + c.getCardValue() + ".png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
        iv.setFitWidth(137);
        iv.setFitHeight(192);
        iv.setPreserveRatio(true);
        UiStyleUtil.applyRoundedCardClip(iv, 137, 192, 18);

        StackPane cardPane = new StackPane(iv);
        cardPane.setPrefSize(137, 192);
        cardPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: green;" +
                        "-fx-border-width: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        Button cardBtn = new Button();
        cardBtn.setGraphic(cardPane);
        cardBtn.setPadding(javafx.geometry.Insets.EMPTY);
        cardBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        cardBtn.setOnAction(e -> cardStack.layCard(c, cardBtn, player));

        ScaleTransition stEnter = new ScaleTransition(Duration.millis(200), cardPane);
        ScaleTransition stExit = new ScaleTransition(Duration.millis(200), cardPane);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.BLACK);

        cardBtn.setOnMouseEntered(e -> {
            stExit.stop();
            stEnter.setToX(1.1);
            stEnter.setToY(1.1);
            stEnter.playFromStart();
            cardPane.setEffect(shadow);
        });

        cardBtn.setOnMouseExited(e -> {
            stEnter.stop();
            stExit.setToX(1.0);
            stExit.setToY(1.0);
            stExit.playFromStart();
            cardPane.setEffect(null);
        });

        return cardBtn;
    }

    /**
     * Creates the close button in the top-right corner.
     */
    public void addCloseButton(StackPane root, Stage stage) {
        Button closeBtn = new Button("X");
        closeBtn.setPrefSize(40, 40);
        closeBtn.setPadding(javafx.geometry.Insets.EMPTY);
        closeBtn.setAlignment(Pos.CENTER);

        closeBtn.setStyle(
                "-fx-background-color: #d32f2f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;"
        );

        closeBtn.setOnAction(e -> onSceneClose());
        stage.setOnCloseRequest(ev -> {
            ev.consume();
            onSceneClose();
        });

        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10));

        root.getChildren().add(closeBtn);
    }

    /**
     * Creates the draw stack button.
     */
    public void createWithdrawalStack(StackPane root) {
        withdrawalButton = new Button();
        withdrawalButton.setPrefSize(137, 192);
        withdrawalButton.setPadding(Insets.EMPTY);
        withdrawalButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        String path = "/htl/steyr/uno/Uno_Cards/backside.png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
        iv.setFitWidth(137);
        iv.setFitHeight(192);
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(true);
        withdrawalButton.setGraphic(iv);

        StackPane.setAlignment(withdrawalButton, Pos.CENTER_LEFT);
        StackPane.setMargin(withdrawalButton, new Insets(0, 0, 0, 400));

        ScaleTransition hoverIn = new ScaleTransition(Duration.millis(150), withdrawalButton);
        hoverIn.setToX(1.1);
        hoverIn.setToY(1.1);

        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(150), withdrawalButton);
        hoverOut.setToX(1.0);
        hoverOut.setToY(1.0);

        withdrawalButton.setOnMouseEntered(e -> {
            hoverOut.stop();
            hoverIn.playFromStart();
        });
        withdrawalButton.setOnMouseExited(e -> {
            hoverIn.stop();
            hoverOut.playFromStart();
        });

        withdrawalButton.setOnAction(e -> {
            if (gameTurnResponse != null && gameTurnResponse.drawPenaltyValue() > 0) {
                getGameLogic().requestCard(gameTurnResponse.drawPenaltyValue());
            } else {
                getGameLogic().requestCard(1);
            }
        });

        root.getChildren().add(withdrawalButton);
    }

    /**
     * Creates the "Say UNO" button.
     */
    private void createSayUnoButton(StackPane root) {
        sayUnoButton = new Button("UNO!");
        sayUnoButton.setPrefSize(100, 50);
        sayUnoButton.setAlignment(Pos.CENTER_LEFT);
        sayUnoButton.setPadding(new Insets(8, 18, 8, 14));
        sayUnoButton.setStyle(
                "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: #FFC107;" +
                        "-fx-text-fill: #000000;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-alignment: center"
        );
        sayUnoButton.setDisable(true);
        sayUnoButton.setMouseTransparent(false);
        sayUnoButton.setFocusTraversable(true);
        sayUnoButton.setPickOnBounds(true);

        sayUnoButton.setOnMouseEntered(e -> {
            if (!sayUnoButton.isDisabled()) {
                new Timeline(new KeyFrame(Duration.millis(150),
                        new KeyValue(sayUnoButton.scaleXProperty(), 1.15),
                        new KeyValue(sayUnoButton.scaleYProperty(), 1.15)
                )).play();
            }
        });

        sayUnoButton.setOnMouseExited(e -> new Timeline(new KeyFrame(Duration.millis(150),
                new KeyValue(sayUnoButton.scaleXProperty(), 1.0),
                new KeyValue(sayUnoButton.scaleYProperty(), 1.0)
        )).play());

        sayUnoButton.setOnAction(e -> {
            if (!unoTimeActive) return;

            new Timeline(
                    new KeyFrame(Duration.millis(100),
                            new KeyValue(sayUnoButton.scaleXProperty(), 0.95),
                            new KeyValue(sayUnoButton.scaleYProperty(), 0.95)
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(sayUnoButton.scaleXProperty(), 1.0),
                            new KeyValue(sayUnoButton.scaleYProperty(), 1.0)
                    )
            ).play();

            unoTimeActive = false;
            if (unoCountdownTimer != null) {
                unoCountdownTimer.stop();
            }
            sayUnoButton.setDisable(true);
            getGameLogic().sayUno();
        });

        StackPane.setAlignment(sayUnoButton, Pos.CENTER);
        StackPane.setMargin(sayUnoButton, new Insets(0, -300, 0, 0));

        root.getChildren().add(sayUnoButton);
        sayUnoButton.toFront();
    }

    /**
     * Activates the UNO countdown and gives the player 3 seconds to say UNO.
     */
    public void startUnoCountdown() {
        Platform.runLater(() -> {
            if (unoTimeActive) {
                return;
            }

            unoTimeActive = true;
            sayUnoButton.setDisable(false);
            sayUnoButton.toFront();

            if (unoCountdownTimer != null) {
                unoCountdownTimer.stop();
            }

            unoCountdownTimer = new Timeline(
                    new KeyFrame(Duration.seconds(3), e -> {
                        unoTimeActive = false;
                        sayUnoButton.setDisable(true);
                        getGameLogic().forgotToSayUno();
                    })
            );
            unoCountdownTimer.setCycleCount(1);
            unoCountdownTimer.play();
        });
    }

    public boolean isUnoTimeActive() {
        return unoTimeActive;
    }

    public void cancelUnoCountdown() {
        Platform.runLater(() -> {
            if (unoCountdownTimer != null) {
                unoCountdownTimer.stop();
            }
            unoTimeActive = false;
            if (sayUnoButton != null) {
                sayUnoButton.setDisable(true);
            }
        });
    }

    /**
     * Indicates that the draw stack is empty by showing the emptyStack.png image.
     */
    public void showEmptyDrawStack() {
        Platform.runLater(() -> {
            if (withdrawalButton != null && withdrawalButton.getGraphic() instanceof ImageView iv) {
                String path = "/htl/steyr/uno/Uno_Cards/emptyStack.png";
                try {
                    Image emptyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
                    iv.setImage(emptyImage);
                } catch (NullPointerException e) {
                    System.err.println("Warnung: emptyStack.png nicht gefunden.");
                }
            }
        });
    }

    /**
     * Restores the original draw stack image (backside.png).
     */
    public void restoreDrawStackImage() {
        Platform.runLater(() -> {
            if (withdrawalButton != null && withdrawalButton.getGraphic() instanceof ImageView iv) {
                String path = "/htl/steyr/uno/Uno_Cards/backside.png";
                try {
                    Image backSideImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
                    iv.setImage(backSideImage);
                } catch (NullPointerException e) {
                    System.err.println("Fehler: backside.png nicht gefunden.");
                }
            }
        });
    }

    public void setCurrentTurneLabel(boolean a) {
        if (yourTurnLabel == null) {
            return;
        }

        Platform.runLater(() -> yourTurnLabel.setText(a ? "Your Turn!" : ""));
    }

    /**
     * Shows the player ranking once the game has ended.
     */
    public void showGameOverOverlay(GameOverResponse msg) {
        if (msg == null || gameOverOverlayShown || root == null) {
            return;
        }
        gameOverOverlayShown = true;

        VBox rankingList = new VBox(8);
        rankingList.getStyleClass().add("game-over-ranking-list");

        int rank = 1;
        for (Player rankedPlayer : msg.getPlayers()) {
            if (rankedPlayer == null || rankedPlayer.getUsername() == null) {
                continue;
            }

            HBox row = new HBox();
            row.getStyleClass().add("game-over-ranking-row");

            Label rankLabel = new Label(rank + ".");
            rankLabel.getStyleClass().add("game-over-rank-label");

            Label nameLabel = new Label(rankedPlayer.getUsername());
            nameLabel.getStyleClass().add("game-over-name-label");

            row.getChildren().addAll(rankLabel, nameLabel);

            if (msg.isLeftPlayer(rankedPlayer.getUsername())) {
                row.getStyleClass().add("game-over-ranking-row-left");
            }

            rankingList.getChildren().add(row);
            rank++;
        }

        Label titleLabel = new Label("Spiel beendet - Ranking");
        titleLabel.getStyleClass().add("game-over-title");

        Button backToLobbyButton = new Button("Zurück zur Lobby");
        backToLobbyButton.getStyleClass().add("game-over-back-button");
        backToLobbyButton.setOnAction(e -> switchBackToLobby());

        VBox panel = new VBox(16, titleLabel, rankingList, backToLobbyButton);
        panel.getStyleClass().add("game-over-panel");
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(500);

        StackPane overlay = new StackPane(panel);
        overlay.getStyleClass().add("game-over-overlay");
        overlay.setPickOnBounds(true);
        overlay.setOnMouseClicked(e -> e.consume());

        root.getChildren().add(overlay);
        overlay.toFront();
    }

    /**
     * Switches back to the lobby after the game ends.
     */
    private void switchBackToLobby() {
        try {
            if (client.getConn() != null) {
                client.getConn().leaveLobby();
            }

            Stage stage = new Stage();
            Stage currentStage = (Stage) root.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));
            LobbyController controller = new LobbyController(client);
            loader.setController(controller);

            client.setLobbyController(controller);
            client.setLobbyWaitController(null);
            client.setGameTable(null);

            Scene scene = new Scene(loader.load());
            UiStyleUtil.applyGlobalFocusStyle(scene);

            stage.setTitle("UNO - Lobby");
            stage.setScene(scene);
            UiStyleUtil.setAppIcon(stage);
            stage.setMaximized(true);
            stage.show();

            currentStage.close();
        } catch (IOException e) {
            System.err.println("Fehler beim Zurueckkehren zur Lobby: " + e.getMessage());
        }
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public Client getClient() {
        return client;
    }

    public Player getPlayer() {
        return player;
    }

    public CardStack getCardStack() {
        return cardStack;
    }

    public StackPane getRoot() {
        return root;
    }

    public ArrayList<EnemyDisplayController> getEnemyControllers() {
        return enemyControllers;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public GameTurnResponse getGameTurnResponse() {
        return gameTurnResponse;
    }

    public void setGameTurnResponse(GameTurnResponse gameTurnResponse) {
        this.gameTurnResponse = gameTurnResponse;
    }

    public Label getYourTurnLabel() {
        return yourTurnLabel;
    }
}