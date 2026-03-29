package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.exceptions.InvalidCardException;
import htl.steyr.uno.client.Client;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GameTable implements Initializable {

    private final Client client;
    @FXML private StackPane root;
    CardStack centralStack = new CardStack();
    private final StartGameResponse startGameResponse;
    private GameLogic gameLogic;
    private Player player;

    @FXML private StackPane enemy1;
    @FXML private StackPane enemy2;
    @FXML private StackPane enemy3;

    ArrayList<EnemyDisplayController> enemyControllers = new ArrayList<>();


    public GameTable(Client client, StartGameResponse msg) {
        this.client = client;
        this.startGameResponse = msg;
        player = new Player(getClient().getConn().getUser());
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
        StackPane.setAlignment(centralStack.getVisual(), javafx.geometry.Pos.CENTER);
        root.getChildren().add(centralStack.getVisual());
    }

    public void setEnemies() {
        List<Enemy> enemies = player.getEnemies();
        int myIndex = player.getPlayerIndex();
        int totalPlayers = enemies.size() + 1;

        List<Enemy> sorted = new ArrayList<>(enemies);
        sorted.sort((a, b) -> {
            int ai = (a.getPlayerIndex() - myIndex + totalPlayers) % totalPlayers;
            int bi = (b.getPlayerIndex() - myIndex + totalPlayers) % totalPlayers;
            return Integer.compare(ai, bi);
        });

        StackPane[] slotOrder = { enemy3, enemy1, enemy2 };

        int[] slotIndices = switch (sorted.size()) {
            case 1 -> new int[]{ 1 };           // nur enemy1
            case 2 -> new int[]{ 0, 2 };        // enemy3, enemy2
            case 3 -> new int[]{ 0, 1, 2 };     // enemy3, enemy1, enemy2
            default -> new int[]{};
        };

        for (int i = 0; i < slotIndices.length && i < sorted.size(); i++) {
            Enemy enemy = sorted.get(i);
            StackPane slot = slotOrder[slotIndices[i]];
            EnemyDisplayController ctrl = addPlayer(slot, enemy.getUsername(), enemy.getImageBytes(), enemy.getHandSize());
            if (ctrl != null) {
                enemyControllers.add(ctrl);
            }
        }
    }


    private EnemyDisplayController addPlayer(StackPane slot, String name, byte[] imageData, int cardCount) {
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
            slot.getChildren().setAll(panel);
            return ctrl;
        } catch (IOException e) {
            System.err.println("Fehler beim Laden von enemy.fxml");
            return null;
        }
    }



    private void onSceneClose() {
        client.setGameTable(null);
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }


    public void open(){
//        ArrayList<Card> myHand = new ArrayList<>();
//        myHand.add(new Card(5, "red"));
//        myHand.add(new Card(9, "blue"));
//        myHand.add(new Card(2, "green"));
//        myHand.add(new Card(8, "yellow"));
//        myHand.add(new Card(12, "red"));
//        myHand.add(new Card(13, "black"));
//        myHand.add(new Card(14, "black"));
//
//        Player player = new Player("Max", true, myHand, getPlayer().getEnemies(), 0, null);


        player.displayPlayerHand(root, player, centralStack);
    }



    public void addCloseButton(StackPane root, Stage stage) {

        Button closeBtn = new Button("X"); // X to make it look like a Close Button
        closeBtn.setPrefSize(40, 40);    //make it smaller
        closeBtn.setPadding(javafx.geometry.Insets.EMPTY);
        closeBtn.setAlignment(javafx.geometry.Pos.CENTER);

        // red/white style for easy visability
        closeBtn.setStyle(
                "-fx-background-color: #d32f2f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;"  //make it appear round
        );

        closeBtn.setOnAction(e -> stage.close());
        // only call onSceneClose when the window is actually closed by the user
        stage.setOnCloseRequest(ev -> onSceneClose());

        //align top right on the stackpane
        StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10));

        root.getChildren().add(closeBtn);
    }

    public void createWithdrawalStack(StackPane root) {
        Button withdrawalButton = new Button();
        withdrawalButton.setPrefSize(137, 192);
        withdrawalButton.setPadding(Insets.EMPTY);
        withdrawalButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        String path = "/htl/steyr/uno/Uno_Cards/backside.png";
        ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
        iv.setFitWidth(137);
        iv.setFitHeight(192);
        iv.setPreserveRatio(true);
        iv.setMouseTransparent(true); // wichtig
        withdrawalButton.setGraphic(iv);

        StackPane.setAlignment(withdrawalButton, Pos.CENTER_LEFT);
        StackPane.setMargin(withdrawalButton, new Insets(0, 0, 0, 400));

        // Hover Animation
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

        // Button Action
        withdrawalButton.setOnAction(e -> {
            System.out.println("Draw Button clicked!");
            if(getPlayer().isCurrentTurn()){
                int i = 0;
                for(Card c : player.getHand()){
                    if(Objects.equals(c.getCardColour(), getCardStack().getTopCard().getCardColour())
                            || c.getCardValue() == getCardStack().getTopCard().getCardValue()
                            || c.getCardValue() == 13
                            || c.getCardValue() == 14){
                        i++;
                    }
                }
                if(i > 0){
                    getGameLogic().requestCard(1);
                }
            }
        });

        root.getChildren().add(withdrawalButton);

        // Wichtig: nach allen Nodes -> Button nach vorne bringen
        Platform.runLater(withdrawalButton::toFront);
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
        return centralStack;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

}
