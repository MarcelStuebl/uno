package htl.steyr.uno;

import htl.steyr.uno.Lobby.LobbyWaitController;
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


public class LobbyController implements Initializable {

    @FXML private Button createPartyButton;
    @FXML private Button joinPartyButton;
    @FXML private TextField partyCodeField;
    @FXML private ImageView profileImageView;

    private Client client;
    private LobbyInfoResponse lobby;


    public LobbyController(Client client) {
        this.client = client;
    }


    /**
     *  Calls the onSceneClose funktion to close the Client when the Scene is closed
     * @param url
     * @param resourceBundle
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
    }


    private void setProfileImage() {
        byte[] imageData = getClient().getConn().getUser().getProfileImageData();
        Image profileImage;
        if (imageData == null || imageData.length == 0) {
            profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")), 50, 50, true, true);
        } else {
            profileImage = new Image(new ByteArrayInputStream(imageData), 50, 50, true, true);
        }
        profileImageView.setImage(profileImage);
    }

    @FXML
    public void onProfileImageClicked(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Profilbild auswählen");

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilddateien", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) profileImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            int maxFileSize = 10 * 1024 * 1024;
            if (file.length() > maxFileSize) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Datei zu groß");
                alert.setHeaderText(null);
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
                alert.setHeaderText(null);
                alert.setContentText("Das ausgewählte Bild konnte nicht geladen werden. Bitte versuchen Sie es erneut.");
                alert.showAndWait();
            }
        }
    }



    /**
     * Closes the Client when the Scene is closed
     */
    private void onSceneClose() {
        if (getClient().getConn() != null) {
            getClient().getConn().close();
        }
    }

    public void joinPartyFailed(LobbyJoinRefusedResponse msg) {
        if (msg.lobbyInfo().status() == 1) {
            System.out.println("Lobby is full. Please try again.");
        } else if (msg.lobbyInfo().status() == 2) {
            System.out.println("Game already started. Please try again.");
        } else {
            System.out.println("Unknown error. Please try again.");
        }
        // @TODO: show error message in UI
    }


    /**
     * Generats a new LobbyCode used to join s lobby
     * @param actionEvent
     */
    @FXML
    private void onCreatePartyButtonClicked(ActionEvent actionEvent) {
        client.createLobby();
    }

    /**
     * Calls switchToLobbyWait when create or join was a success
     * @param lobby
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
     *  Joins a new game with all players that are in the party
     * @param actionEvent
     */
    @FXML
    private void onJoinButtonClicked(ActionEvent actionEvent) throws IOException {
        String input = partyCodeField.getText().trim();
        
        if (input.isEmpty()) {
            // @TODO: Show error message
            return;
        }
        
        try {
            int lobbyId = Integer.parseInt(input);
            if (lobbyId <= 100000 || lobbyId >= 999999) {
                // @TODO: Show error message
            } else {
                client.joinLobby(lobbyId);
            }
        } catch (NumberFormatException e) {
            // @TODO: Show error message
        }
    }

    /**
     * Handles a lobby not found response from the server by notifying the user and prompting
     * them to join or create a lobby again.
     */
    public void lobbyNotFound() {
        System.out.println("Invalid lobby ID. Please try again.");
    }


    /**
     * Changes the scene to lobbyWait when joinPartyButton ist clicked
     * @throws IOException
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

        stage.setTitle("WarteLobby");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        thisStage.close();
    }

    /**
     * Logs the acc out / goes back to the login screen when logOutButton is clicked
     * @param actionEvent
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
        stage.setMaximized(true);
        stage.show();
        thisStage.close();
        onSceneClose();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void sendChatMessage(String message) {
        client.sendChatMessage(message);
    }

    public void receiveChatMessage(ReceiveChatMessageResponse msg) {
        String sender = msg.user().getUsername();
        String message = msg.message();
        System.out.println("Received chat message from " + sender + ": " + message);
    }
}
