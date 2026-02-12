package htl.steyr.uno.GameTableClasses;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.IOException;

public class GameTable {

    public void open(Stage stage) {
        makeTable(stage);
        closeButton();

    }


    private void makeTable(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/htl/steyr/uno/gameTable.fxml"));

            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage.setTitle("UNO - Game Table");
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

            // make table response incase of later use
            Pane table = (Pane) root.lookup("#table");
            StackPane stack = (StackPane) root;
            table.prefWidthProperty().bind(stack.widthProperty());
            table.prefHeightProperty().bind(stack.heightProperty());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeButton() {
        Button b = new Button();
        //implement
    }


}
