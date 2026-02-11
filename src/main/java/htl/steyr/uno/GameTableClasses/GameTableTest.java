package htl.steyr.uno.GameTableClasses;

import javafx.application.Application;
import javafx.stage.Stage;

public class GameTableTest extends Application {

    @Override
    public void start(Stage primaryStage) {

        // open gameTable as regular Object
        GameTable gameTable = new GameTable();
        gameTable.open(primaryStage);
    }

    public static void main(String[] args) {
        launch(args); // start javaFX
    }
}
