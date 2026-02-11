package htl.steyr.uno.GameTableClasses;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameTable {

    public void open(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/htl/steyr/uno/gameTable.fxml"));

            Scene scene = new Scene(root);
            stage.setTitle("UNO - Game Table");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
