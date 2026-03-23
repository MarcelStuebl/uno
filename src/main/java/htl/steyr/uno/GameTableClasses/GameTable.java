package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.exceptions.InvalidHandException;
import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GameTable implements Initializable {

    private final Client client;
    @FXML private StackPane root;
    private Stage stage;
    CardStack centralStack = new CardStack();

    public GameTable(Client client) {
        this.client = client;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            try {
                open(stage);
            } catch (InvalidHandException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }


    private void open(Stage stage) throws InvalidHandException {
        StackPane root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/htl/steyr/uno/gameTable.fxml"));
            root = loader.load();

            Scene scene = new Scene(root);
            UiStyleUtil.applyGlobalFocusStyle(scene);
            stage.setScene(scene);
            stage.setTitle("UNO - Game Table");
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Card> myHand = new ArrayList<>();
        myHand.add(new Card(5, "red"));
        myHand.add(new Card(9, "blue"));
        myHand.add(new Card(2, "green"));
        myHand.add(new Card(8, "yellow"));
        myHand.add(new Card(12, "red"));
        myHand.add(new Card(13, "black"));
        myHand.add(new Card(14, "black"));



        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy("Anna", false, 5,1));
        enemies.add(new Enemy("Lukas", false, 7,4));
        enemies.add(new Enemy("Sophie", false, 3,4));


        Player player = new Player("Max",true,myHand,enemies);


        StackPane.setAlignment(centralStack.getVisual(), javafx.geometry.Pos.CENTER);
        root.getChildren().add(centralStack.getVisual());


        player.displayPlayerHand(root, player, centralStack);


        addCloseButton(root, stage);  //for readabiity


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
        onSceneClose();

        //align top right on the stackpane
        StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10));

        root.getChildren().add(closeBtn);
    }
}
