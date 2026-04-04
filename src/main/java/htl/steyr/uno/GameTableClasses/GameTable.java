package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.GameTurnResponse;
import htl.steyr.uno.requests.server.StartGameResponse;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

    //variables used for PlayerHand
    private VBox handVBox; // VBox holding multiple rows of cards
    private int maxCardsPerRow = 30; // maximum cards in one row

    @FXML private StackPane enemy1;
    @FXML private StackPane enemy2;
    @FXML private StackPane enemy3;

    private final ArrayList<EnemyDisplayController> enemyControllers = new ArrayList<>();

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
        });
    }

    private void setupCentralStack() {
        StackPane.setAlignment(cardStack.getVisual(), javafx.geometry.Pos.CENTER);
        root.getChildren().add(cardStack.getVisual());
    }

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

    public void open() {
        handVBox = new VBox();
        handVBox.setAlignment(Pos.BOTTOM_CENTER);
        handVBox.setSpacing(-200); // rows slightly overlap each other
        handVBox.setMouseTransparent(false);

        updatePlayerHandUI();

        StackPane.setAlignment(handVBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(handVBox, new Insets(40));
        root.getChildren().add(handVBox);

        if (withdrawalButton != null) {
            withdrawalButton.toFront();
        }
    }

    // Calculate horizontal spacing between cards in a row
    private double calculateSpacing(int cardCount) {
        double minSpacing = -140; // tighter overlap for crowded rows
        double maxSpacing = -50;  // less overlap for small rows

        if (cardCount <= 5) return maxSpacing;
        if (cardCount >= maxCardsPerRow) return minSpacing;

        // Linear interpolation between min and max
        return maxSpacing - ((cardCount - 5) * (Math.abs(maxSpacing - minSpacing) / (maxCardsPerRow - 5)));
    }

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
            row.setSpacing(spacing); // negative spacing = cards closer together

            // Add cards to row
            for (int i = 0; i < cardsInThisRow; i++) {
                Button cardButton = createCardButton(cards.get(index));
                // Slightly move cards upward for a layered effect
                cardButton.setTranslateY(40 * rowNumber);
                row.getChildren().add(cardButton);
                index++;
            }

            // Slightly shift each row downward so previous rows are visible
            row.setTranslateY(10 * rowNumber);
            rowNumber++;

            handVBox.getChildren().add(row);
        }

        // Make sure the withdrawal button is always on top
        if (withdrawalButton != null) {
            withdrawalButton.toFront();
        }
    }

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

        cardBtn.setOnAction(e -> {
            cardStack.layCard(c, cardBtn, player);
        });

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
                int penaltyValue = gameTurnResponse.drawPenaltyValue();
                getGameLogic().requestCard(penaltyValue);
            } else {
                getGameLogic().requestCard(1);
            }
        });

        root.getChildren().add(withdrawalButton);
    }

    /**
     * Zeigt an, dass der Abhebestapel leer ist, indem das emptyStack.png Image angezeigt wird
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
     * Stellt das ursprüngliche Image des Abhebestapels wieder her (backside.png)
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
}