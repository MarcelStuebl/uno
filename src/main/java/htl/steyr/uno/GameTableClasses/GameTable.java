package htl.steyr.uno.GameTableClasses;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class GameTable {

    public void open(Stage stage) {
        makeTable(stage);

    }


    private void makeTable(Stage stage) {
        StackPane root = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/htl/steyr/uno/gameTable.fxml"));
            root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("UNO - Game Table");
            stage.setFullScreen(true);
            stage.show();

            addCloseButton(root, stage);   // ← ausgelagert

        } catch (IOException e) {
            e.printStackTrace();
        }

//test-------------------------------
        Player player1 = new Player();
        ArrayList<Card> hand = new ArrayList<>();
        hand.add(new Card(3, "red"));
        hand.add(new Card(7, "blue"));
        hand.add(new Card(12, "green")); // Draw 2
        hand.add(new Card(0, "yellow"));
        hand.add(new Card(10, "red"));   // Skip
        hand.add(new Card(13, "black")); // Choose colour
        hand.add(new Card(5, "green"));
        player1.setPlayerHand(hand);

    }
    private void addCloseButton(StackPane root, Stage stage) {

        Button closeBtn = new Button();

        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(
                new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/htl/steyr/uno/testMHORETH/closeButton_TEST.png")
                )
        );

        iv.setFitWidth(100);
        iv.setPreserveRatio(true);

        closeBtn.setGraphic(iv);
        closeBtn.setStyle("-fx-background-color: transparent;");
        closeBtn.setPadding(javafx.geometry.Insets.EMPTY);
        closeBtn.setOnAction(e -> stage.close());

        StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(15));

        root.getChildren().add(closeBtn);
    }




}
