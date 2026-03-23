package htl.steyr.uno.Lobby;

import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.UiStyleUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LobbyTestApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("../../../../../resources/htl/steyr/uno/lobby.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        UiStyleUtil.applyGlobalFocusStyle(scene);
        stage.setTitle("Lobby");
        stage.setScene(scene);
        //stage.setFullScreen(true);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // start javaFX
    }
}
