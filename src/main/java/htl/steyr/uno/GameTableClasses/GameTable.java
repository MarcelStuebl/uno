package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.controller.CardStackController;
import htl.steyr.uno.GameTableClasses.controller.EnemyDisplayController;
import htl.steyr.uno.GameTableClasses.controller.GameChatController;
import htl.steyr.uno.GameTableClasses.controller.GameOverController;
import htl.steyr.uno.GameTableClasses.controller.GameThemeController;
import htl.steyr.uno.GameTableClasses.controller.PlayerHandController;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.GameOverResponse;
import htl.steyr.uno.requests.server.GameTurnResponse;
import htl.steyr.uno.requests.server.EmojiReactionResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import htl.steyr.uno.requests.server.StartGameResponse;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameTable implements Initializable {

    private final Client client;
    @FXML
    private StackPane root;
    private GameThemeController themeController;
    private GameChatController chatController;
    private GameOverController gameOverController;
    CardStackController cardStack = new CardStackController(this);
    private final StartGameResponse startGameResponse;
    private GameLogic gameLogic;
    private Player player;
    private GameTurnResponse gameTurnResponse;
    private Button withdrawalButton;
    private boolean withdrawalButtonCooldown = false;
    @FXML
    private Label yourTurnLabel;

    private PlayerHandController handController;

    @FXML
    private StackPane enemy1;
    @FXML
    private StackPane enemy2;
    @FXML
    private StackPane enemy3;

    private final ArrayList<EnemyDisplayController> enemyControllers = new ArrayList<>();

    private Button sayUnoButton; // Button to say "UNO"
    private boolean unoTimeActive = false; // Indicates if the UNO countdown is active
    private Timeline unoCountdownTimer; // Timer for the UNO countdown

    private StackPane overlayLayer;
    private Pane reactionPopupLayer;


    public GameTable(Client client, StartGameResponse msg) {
        this.client = client;
        this.startGameResponse = msg;
        this.player = new Player(getClient().getConn().getUser());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        themeController = new GameThemeController(root);
        themeController.initializeBackground();
        updatePlayerFromStartGameResponse();
        gameLogic = new GameLogic(this);
        handController = new PlayerHandController(this, client);
        gameOverController = new GameOverController(this, client);

        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("UNO - Game Table");
            stage.setMaximized(true);
            stage.setResizable(true);

            setupCentralStack();

            overlayLayer = new StackPane();
            overlayLayer.setPickOnBounds(false);
            overlayLayer.setMouseTransparent(false);
            root.getChildren().add(overlayLayer);
            overlayLayer.toFront();

            chatController = new GameChatController(client, root, overlayLayer);
            chatController.install(stage);

            if (handController.getHandNode() != null) {
                handController.getHandNode().toFront();
            }
            if (withdrawalButton != null) {
                withdrawalButton.toFront();
            }
            if (sayUnoButton != null) {
                sayUnoButton.toFront();
            }
            reactionPopupLayer = new Pane();
            reactionPopupLayer.setPickOnBounds(false);
            reactionPopupLayer.setMouseTransparent(true);
            overlayLayer.getChildren().add(reactionPopupLayer);

            addCloseButton(root, stage);
            createWithdrawalStack(root);
            createSayUnoButton(root);
            themeController.installThemePicker();

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

            gameLogic.sendReadyToStart();
        });
    }

    public void receiveEmojiReaction(EmojiReactionResponse msg) {
        Platform.runLater(() -> {
            if (msg == null || msg.emoji() == null || msg.emoji().isBlank()) {
                return;
            }
            showFloatingReaction(msg.emoji());
        });
    }

    private void showFloatingReaction(String emoji) {
        if (reactionPopupLayer == null || emoji == null) {
            return;
        }

        ImageView popup = PlayerHandController.createReactionImageView(emoji, 88);
        popup.setMouseTransparent(true);
        popup.setManaged(false);
        reactionPopupLayer.getChildren().add(popup);
        double maxX = Math.max(0, reactionPopupLayer.getWidth() - 100);
        double maxY = Math.max(0, reactionPopupLayer.getHeight() - 100);
        popup.setLayoutX(ThreadLocalRandom.current().nextDouble(maxX + 1));
        popup.setLayoutY(ThreadLocalRandom.current().nextDouble(maxY + 1));
        overlayLayer.toFront();
        reactionPopupLayer.toFront();

        Timeline popIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(popup.scaleXProperty(), 0.35),
                        new KeyValue(popup.scaleYProperty(), 0.35),
                        new KeyValue(popup.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(180),
                        new KeyValue(popup.scaleXProperty(), 1.15),
                        new KeyValue(popup.scaleYProperty(), 1.15),
                        new KeyValue(popup.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(320),
                        new KeyValue(popup.scaleXProperty(), 1),
                        new KeyValue(popup.scaleYProperty(), 1))
        );
        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), popup);
        fadeOut.setDelay(Duration.millis(1300));
        fadeOut.setToValue(0);
        TranslateTransition floatUp = new TranslateTransition(Duration.seconds(2), popup);
        floatUp.setByY(-75);
        ParallelTransition animation = new ParallelTransition(popIn, fadeOut, floatUp);
        animation.setOnFinished(event -> reactionPopupLayer.getChildren().remove(popup));
        animation.play();
    }

    private void updatePlayerFromStartGameResponse() {
        for (Enemy enemy : startGameResponse.enemies()) {
            if (!Objects.equals(enemy.getUsername(), player.getUsername())) {
                player.getEnemies().add(enemy);
                break;
            }
        }
    }



    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        if (chatController != null) {
            chatController.receiveMessage(msg);
        }
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

        StackPane[] slotOrder = {enemy3, enemy1, enemy2};

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
                    if (enemy != null && enemy.getUsername() != null && enemy.getUsername().equals(ctrl.getUsername()) && activePlayerIndex.equals(enemy.getPlayerIndex())) {
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
            profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")), 50, 50, true, true);
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
        handController.open();

        if (withdrawalButton != null) {
            withdrawalButton.toFront();
        }
        if (sayUnoButton != null) {
            sayUnoButton.toFront();
        }
    }

    /**
     * Updates the player's hand card display.
     */
    public void updatePlayerHandUI() {
        handController.updatePlayerHandUI();
        if (handController.getHandNode() != null) {
            handController.getHandNode().toFront();
        }
        if (withdrawalButton != null) {
            withdrawalButton.toFront();
        }
        if (sayUnoButton != null) {
            sayUnoButton.toFront();
        }
        if (handController.getOwnProfileDock() != null) {
            handController.getOwnProfileDock().toFront();
        }
    }

    /**
     * Creates the close button in the top-right corner.
     */
    public void addCloseButton(StackPane root, Stage stage) {
        Button closeBtn = new Button("X");
        closeBtn.setPrefSize(40, 40);
        closeBtn.setPadding(javafx.geometry.Insets.EMPTY);
        closeBtn.setAlignment(Pos.CENTER);

        closeBtn.setStyle("-fx-background-color: #d32f2f;" + "-fx-text-fill: white;" + "-fx-font-size: 18;" + "-fx-font-weight: bold;" + "-fx-background-radius: 20;");

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
            // Prevent spam by checking cooldown flag
            if (withdrawalButtonCooldown) {
                return;
            }

            // Set cooldown
            withdrawalButtonCooldown = true;

            if (gameTurnResponse != null && gameTurnResponse.drawPenaltyValue() > 0) {
                getGameLogic().requestCard(gameTurnResponse.drawPenaltyValue());
            } else {
                getGameLogic().requestCard(1);
            }

            // Re-enable button after 1 second
            Timeline cooldownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> withdrawalButtonCooldown = false));
            cooldownTimer.play();
        });

        root.getChildren().add(withdrawalButton);
    }

    /**
     * Creates the "Say UNO" button.
     */
    private void createSayUnoButton(StackPane root) {
        sayUnoButton = new Button("Say UNO");
        sayUnoButton.setPrefSize(100, 50);
        sayUnoButton.setAlignment(Pos.CENTER_LEFT);
        sayUnoButton.setPadding(new Insets(8, 18, 8, 14));
        sayUnoButton.setStyle("-fx-font-size: 14;" + "-fx-font-weight: bold;" + "-fx-background-color: #FFC107;" + "-fx-text-fill: #000000;" + "-fx-padding: 8px 16px;" + "-fx-background-radius: 8;" + "-fx-alignment: center");
        sayUnoButton.setDisable(true);
        sayUnoButton.setMouseTransparent(false);
        sayUnoButton.setFocusTraversable(true);
        sayUnoButton.setPickOnBounds(true);

        sayUnoButton.setOnMouseEntered(e -> {
            if (!sayUnoButton.isDisabled()) {
                new Timeline(new KeyFrame(Duration.millis(150), new KeyValue(sayUnoButton.scaleXProperty(), 1.15), new KeyValue(sayUnoButton.scaleYProperty(), 1.15))).play();
            }
        });

        sayUnoButton.setOnMouseExited(e -> new Timeline(new KeyFrame(Duration.millis(150), new KeyValue(sayUnoButton.scaleXProperty(), 1.0), new KeyValue(sayUnoButton.scaleYProperty(), 1.0))).play());

        sayUnoButton.setOnAction(e -> {
            if (!unoTimeActive) return;

            new Timeline(new KeyFrame(Duration.millis(100), new KeyValue(sayUnoButton.scaleXProperty(), 0.95), new KeyValue(sayUnoButton.scaleYProperty(), 0.95)), new KeyFrame(Duration.millis(200), new KeyValue(sayUnoButton.scaleXProperty(), 1.0), new KeyValue(sayUnoButton.scaleYProperty(), 1.0))).play();

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
     * <p>
     * FIX: The entire method is now wrapped in Platform.runLater() because it is
     * called from CardStack.layCard() which runs on the JavaFX thread via a card
     * button click — but the UNO countdown involves Timeline and Button.setDisable()
     * which must always execute on the FX thread. Previously, inconsistent thread
     * context caused the button to appear enabled but be unresponsive on the first attempt.
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

            // Single KeyFrame fires after exactly 3 seconds — simpler and more reliable
            // than setCycleCount(3) with a 1-second frame, which had edge cases where
            // forgotToSayUno() was called in the wrong tick.
            unoCountdownTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                unoTimeActive = false;
                sayUnoButton.setDisable(true);
            }));
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
        gameOverController.showOverlay(msg);
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

    public CardStackController getCardStack() {
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

    public record ChatMessage(String sender, String text) {
    }
}