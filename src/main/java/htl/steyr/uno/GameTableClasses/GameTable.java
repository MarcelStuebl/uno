package htl.steyr.uno.GameTableClasses;

import htl.steyr.uno.GameTableClasses.exceptions.InvalidHandException;
import htl.steyr.uno.client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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


    public void open(Stage stage) throws InvalidHandException {
        makeTable(stage);

    }


    private void makeTable(Stage stage) throws InvalidHandException {
        StackPane root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/htl/steyr/uno/gameTable.fxml"));
            root = loader.load();

            stage.setScene(new Scene(root));
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
        enemies.add(new Enemy("Anna", false, 5));
        enemies.add(new Enemy("Lukas", false, 7));
        enemies.add(new Enemy("Sophie", false, 3));


        Player player = new Player("Max",true,myHand,enemies);


        StackPane.setAlignment(centralStack.getVisual(), javafx.geometry.Pos.CENTER);
        root.getChildren().add(centralStack.getVisual());



        player.showPlayerHand(root, player, centralStack);

        player.testPrintHand();

        addCloseButton(root, stage);  //for readabiity


    }

    private void addCloseButton(StackPane root, Stage stage) {

        Button closeBtn = new Button("X"); // Symmetrisches X
        closeBtn.setPrefSize(40, 40);      // klein
        closeBtn.setPadding(javafx.geometry.Insets.EMPTY);
        closeBtn.setAlignment(javafx.geometry.Pos.CENTER);

        // Einfach rot/weiß für Sichtbarkeit
        closeBtn.setStyle(
                "-fx-background-color: #d32f2f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;"  // rund
        );

        closeBtn.setOnAction(e -> stage.close());
        onSceneClose();


        // Oben rechts im StackPane
        StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10));

        root.getChildren().add(closeBtn);
    }



}
