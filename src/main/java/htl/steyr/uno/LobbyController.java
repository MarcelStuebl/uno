package htl.steyr.uno;

import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import htl.steyr.uno.requests.server.LobbyJoinRefusedResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller-Klasse für die Lobby-Ansicht der UNO-Anwendung.
 * <p>
 * Diese Klasse verwaltet:
 * <ul>
 *     <li>Erstellen und Beitreten zu Lobbys</li>
 *     <li>Anzeigen von Spielerstatistiken</li>
 *     <li>Profilbild-Verwaltung</li>
 *     <li>Navigation zwischen Szenen</li>
 *     <li>Empfang von Serverantworten</li>
 * </ul>
 */
public class LobbyController implements Initializable {

    @FXML private Button createPartyButton;
    @FXML private Button joinPartyButton;
    @FXML private TextField partyCodeField;
    @FXML private Label errorLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label gamesWonLabel;
    @FXML private Label winRateLabel;
    @FXML private ImageView profileImageView;

    /** Client zur Kommunikation mit dem Server */
    private Client client;

    /** Aktuelle Lobby-Informationen */
    private LobbyInfoResponse lobby;

    /**
     * Konstruktor für den LobbyController.
     *
     * @param client der Client für Serverkommunikation
     */
    public LobbyController(Client client) {
        this.client = client;
    }

    /**
     * Initialisiert die Benutzeroberfläche nach dem Laden.
     * <p>
     * Setzt:
     * <ul>
     *     <li>Event-Handler beim Schließen des Fensters</li>
     *     <li>Profilbild</li>
     *     <li>Spielstatistiken</li>
     * </ul>
     *
     * @param url Ressourcen-URL
     * @param resourceBundle Ressourcen-Bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            createPartyButton.getScene().getWindow().setOnCloseRequest(event -> {
                onSceneClose();
            });
        });

        setProfileImage();
        getClient().setLobbyController(this);

        int wonGames = getClient().getConn().getUser().getGamesWon();
        int lostGames = getClient().getConn().getUser().getGamesLost();

        gamesWonLabel.setText("Siege: " + wonGames);
        gamesPlayedLabel.setText("Spiele: " + (wonGames + lostGames));
    }

    /**
     * Setzt das Profilbild des Benutzers.
     * <p>
     * Falls kein Bild vorhanden ist, wird ein Standardbild verwendet.
     */
    private void setProfileImage() {
        byte[] imageData = getClient().getConn().getUser().getProfileImageData();
        Image profileImage;

        if (imageData == null || imageData.length == 0) {
            profileImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")),
                    50, 50, true, true
            );
        } else {
            profileImage = new Image(new ByteArrayInputStream(imageData), 50, 50, true, true);
        }

        profileImageView.setImage(profileImage);
    }

    /**
     * Wird ausgelöst, wenn das Profilbild angeklickt wird.
     * <p>
     * Öffnet einen FileChooser zum Auswählen eines neuen Bildes
     * und lädt dieses hoch.
     *
     * @param event MouseEvent
     */
    @FXML
    public void onProfileImageClicked(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Profilbild auswählen");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Bilddateien", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) profileImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            int maxFileSize = 10 * 1024 * 1024;

            if (file.length() > maxFileSize) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Datei zu groß");
                alert.setContentText("Das Profilbild darf maximal 10 MB groß sein.");
                alert.showAndWait();
                return;
            }

            try {
                Image image = new Image(file.toURI().toString(), 60, 60, true, true);
                profileImageView.setImage(image);

                byte[] imageBytes = Files.readAllBytes(file.toPath());
                getClient().getConn().getUser().setProfileImageData(imageBytes);

                setProfileImage();
                getClient().getConn().setProfileImageRequest(imageBytes);

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler beim Laden des Bildes");
                alert.setContentText("Das ausgewählte Bild konnte nicht geladen werden.");
                alert.showAndWait();
            }
        }
    }

    /**
     * Wird beim Schließen der Szene aufgerufen.
     * <p>
     * Schließt die Verbindung zum Server.
     */
    private void onSceneClose() {
        if (getClient().getConn() != null) {
            getClient().getConn().close();
        }
    }

    /**
     * Wird aufgerufen, wenn das Beitreten zu einer Lobby fehlschlägt.
     *
     * @param msg Antwort vom Server mit Fehlerstatus
     */
    public void joinPartyFailed(LobbyJoinRefusedResponse msg) {
        Platform.runLater(() -> {
            if (msg.lobbyInfo().status() == 1) {
                errorLabel.setText("Lobby is full. Please try again.");
            } else if (msg.lobbyInfo().status() == 2) {
                errorLabel.setText("Game already started. Please try again.");
            } else {
                errorLabel.setText("Unknown error. Please try again.");
            }
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });
    }

    /**
     * Wird aufgerufen, wenn der "Create Party"-Button geklickt wird.
     *
     * @param actionEvent Event
     */
    @FXML
    private void onCreatePartyButtonClicked(ActionEvent actionEvent) {
        client.createLobby();
    }

    /**
     * Wird aufgerufen, wenn das Erstellen oder Beitreten zu einer Lobby erfolgreich war.
     *
     * @param lobby Lobby-Informationen
     */
    public void createOrJoinPartySuccess(LobbyInfoResponse lobby) {
        this.lobby = lobby;

        Platform.runLater(() -> {
            try {
                switchToLobbyWait();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Wird aufgerufen, wenn der "Join"-Button geklickt wird.
     *
     * @param actionEvent Event
     * @throws IOException falls ein Fehler beim Laden der Szene auftritt
     */
    @FXML
    private void onJoinButtonClicked(ActionEvent actionEvent) throws IOException {
        String input = partyCodeField.getText().trim();

        if (input.isEmpty()) {
            errorLabel.setText("Please enter a lobby ID.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            int lobbyId = Integer.parseInt(input);

            if (lobbyId <= 100000 || lobbyId >= 999999) {
                errorLabel.setText("Invalid lobby ID. Please try again.");
                errorLabel.setVisible(true);
            } else {
                client.joinLobby(lobbyId);
            }

        } catch (NumberFormatException e) {
            errorLabel.setText("Lobby ID must be a number.");
            errorLabel.setVisible(true);
        }
    }

    /**
     * Wird aufgerufen, wenn keine Lobby gefunden wurde.
     */
    public void lobbyNotFound() {
        System.out.println("Invalid lobby ID. Please try again.");
    }

    /**
     * Wechselt zur Lobby-Warteansicht.
     *
     * @throws IOException falls die FXML-Datei nicht geladen werden kann
     */
    private void switchToLobbyWait() throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) joinPartyButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobbyWait.fxml"));

        LobbyWaitController controller = new LobbyWaitController(client, lobby);
        loader.setController(controller);
        client.setLobbyWaitController(controller);

        Scene scene = new Scene(loader.load());
        UiStyleUtil.applyGlobalFocusStyle(scene);

        stage.setTitle("UNO - WarteLobby");
        stage.setScene(scene);
        UiStyleUtil.setAppIcon(stage);
        stage.setMaximized(true);
        stage.show();

        thisStage.close();
    }

    /**
     * Wird aufgerufen, wenn der Benutzer sich ausloggt.
     *
     * @param actionEvent Event
     * @throws IOException falls die Szene nicht geladen werden kann
     */
    @FXML
    private void onLogoutButtonClicked(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) createPartyButton.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        UiStyleUtil.applyGlobalFocusStyle(scene);
        stage.setTitle("UNO-AnmeldeBildschirm");
        stage.setScene(scene);
        UiStyleUtil.setAppIcon(stage);
        stage.setMaximized(true);
        stage.show();

        thisStage.close();
        onSceneClose();
    }

    /**
     * Gibt den aktuellen Client zurück.
     *
     * @return Client
     */
    public Client getClient() {
        return client;
    }

    /**
     * Setzt den Client.
     *
     * @param client Client
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Sendet eine Chatnachricht an den Server.
     *
     * @param message Nachricht
     */
    private void sendChatMessage(String message) {
        client.sendChatMessage(message);
    }

    /**
     * Verarbeitet eine empfangene Chatnachricht vom Server.
     *
     * @param msg Chatnachricht
     */
    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        String sender = msg.user().getUsername();
        String message = msg.message();
        System.out.println("Received chat message from " + sender + ": " + message);
    }
}