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
            stage.setMaximized(true);
            stage.setResizable(false);
            stage.show();

            addCloseButton(root, stage);   // ← ausgelagert

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Card> myHand = new ArrayList<>();
        myHand.add(new Card(5, "red"));
        myHand.add(new Card(9, "blue"));
        myHand.add(new Card(2, "green"));
        myHand.add(new Card(8, "yellow"));
        myHand.add(new Card(1, "red"));
        myHand.add(new Card(6, "blue"));
        myHand.add(new Card(4, "green"));


        ArrayList<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy("Anna", false, 5));
        enemies.add(new Enemy("Lukas", false, 7));
        enemies.add(new Enemy("Sophie", false, 3));


        Player player = new Player("Max",true,myHand,enemies);


        player.testPrintHand();
        player.showPlayerHand(root,player);


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


        // Oben rechts im StackPane
        StackPane.setAlignment(closeBtn, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new javafx.geometry.Insets(10));

        root.getChildren().add(closeBtn);
    }






}
