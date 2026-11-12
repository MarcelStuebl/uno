package htl.steyr.uno.GameTableClasses.controller;

import htl.steyr.uno.GameTableClasses.GameTable;
import htl.steyr.uno.GameTableClasses.Player;
import htl.steyr.uno.HelloApplication;
import htl.steyr.uno.LobbyController;
import htl.steyr.uno.UiStyleUtil;
import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.GameOverResponse;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public final class GameOverController {
    private final GameTable gameTable;
    private final Client client;
    private boolean overlayShown;

    public GameOverController(GameTable gameTable, Client client) {
        this.gameTable = gameTable;
        this.client = client;
    }

    public void showOverlay(GameOverResponse response) {
        StackPane root = gameTable.getRoot();
        if (response == null || overlayShown || root == null) {
            return;
        }
        overlayShown = true;

        VBox rankingList = new VBox(8);
        rankingList.getStyleClass().add("game-over-ranking-list");
        int rank = 1;
        for (Player rankedPlayer : response.getPlayers()) {
            if (rankedPlayer == null || rankedPlayer.getUsername() == null) {
                continue;
            }

            HBox row = new HBox();
            row.getStyleClass().add("game-over-ranking-row");
            Label rankLabel = new Label(rank + ".");
            rankLabel.getStyleClass().add("game-over-rank-label");
            Label nameLabel = new Label(rankedPlayer.getUsername());
            nameLabel.getStyleClass().add("game-over-name-label");
            row.getChildren().addAll(rankLabel, nameLabel);
            if (response.isLeftPlayer(rankedPlayer.getUsername())) {
                row.getStyleClass().add("game-over-ranking-row-left");
            }
            rankingList.getChildren().add(row);
            rank++;
        }

        Label titleLabel = new Label("Spiel beendet - Ranking");
        titleLabel.getStyleClass().add("game-over-title");
        Button backToLobbyButton = new Button("Zurück zur Lobby");
        backToLobbyButton.getStyleClass().add("game-over-back-button");
        backToLobbyButton.setOnAction(event -> switchBackToLobby());

        VBox panel = new VBox(16, titleLabel, rankingList, backToLobbyButton);
        panel.getStyleClass().add("game-over-panel");
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(500);

        StackPane overlay = new StackPane(panel);
        overlay.getStyleClass().add("game-over-overlay");
        overlay.setPickOnBounds(true);
        overlay.setOnMouseClicked(event -> event.consume());
        root.getChildren().add(overlay);
        overlay.toFront();
    }

    private void switchBackToLobby() {
        try {
            if (client.getConn() != null) {
                client.getConn().leaveLobby();
            }

            Stage stage = new Stage();
            Stage currentStage = (Stage) gameTable.getRoot().getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));
            LobbyController controller = new LobbyController(client);
            loader.setController(controller);

            client.setLobbyController(controller);
            client.setLobbyWaitController(null);
            client.setGameTable(null);

            Scene scene = new Scene(loader.load());
            UiStyleUtil.applyGlobalFocusStyle(scene);
            stage.setTitle("UNO - Lobby");
            stage.setScene(scene);
            UiStyleUtil.setAppIcon(stage);
            stage.setMaximized(true);
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            System.err.println("Fehler beim Zurueckkehren zur Lobby: " + e.getMessage());
        }
    }
}
