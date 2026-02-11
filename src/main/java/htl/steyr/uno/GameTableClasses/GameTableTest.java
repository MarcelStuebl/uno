package htl.steyr.uno;

import javafx.application.Application;
import javafx.stage.Stage;

public class GameTableTest extends Application {

    @Override
    public void start(Stage primaryStage) {

        // GameTable als normales Objekt öffnen
        GameTable gameTable = new GameTable();
        gameTable.open(primaryStage);
    }

    public static void main(String[] args) {
        launch(args); // startet JavaFX
    }
}
