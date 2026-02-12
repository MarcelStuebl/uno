package htl.steyr.uno.GameTableClasses;

import javafx.application.Application;
import javafx.stage.Stage;

public class GameTableTest extends Application {

    @Override
    public void start(Stage stage) {

        // open gameTable as regular Object
        GameTable gameTable = new GameTable();
        gameTable.open(stage);
    }

    public static void main(String[] args) {
        launch(args); // start javaFX
    }
}
