package htl.steyr.uno;

import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.LobbyInfoResponse;
import htl.steyr.uno.requests.server.LobbyJoinRefusedResponse;
import htl.steyr.uno.requests.server.MatchesStatisticsResponse;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @FXML private StackPane centerPane;
    @FXML private Button statsButton;
    @FXML private Region statsOverlay;
    @FXML private VBox statsPanel;
    @FXML private Label statsSummaryLabel;
    @FXML private ScrollPane statsScrollPane;
    @FXML private VBox matchListBox;

    /** Format für das Datum eines Matches im Stats-Popup */
    private static final DateTimeFormatter MATCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm");

    /** Dauer der Ein- und Ausblendanimation des Stats-Popups */
    private static final Duration STATS_ANIMATION_DURATION = Duration.millis(250);

    /** Größe der Profilbilder der Spieler im Stats-Popup */
    private static final double PLAYER_AVATAR_SIZE = 20;

    /** Client zur Kommunikation mit dem Server */
    private Client client;

    /** Aktuelle Lobby-Informationen */
    private LobbyInfoResponse lobby;

    /** Gibt an, ob das Stats-Popup gerade geöffnet ist */
    private boolean statsOpen = false;

    /** Aktuell laufende Animation des Stats-Popups */
    private ParallelTransition statsTransition;

    /** Standard-Profilbild für Spieler ohne eigenes Bild (wird beim ersten Gebrauch geladen) */
    private Image defaultProfileImage;

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

        // Stats-Popup beim Ein-/Ausfahren nicht über die untere Leiste zeichnen
        Rectangle centerClip = new Rectangle();
        centerClip.widthProperty().bind(centerPane.widthProperty());
        centerClip.heightProperty().bind(centerPane.heightProperty());
        centerPane.setClip(centerClip);
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
     * Wird aufgerufen, wenn der "Stats"-Button geklickt wird.
     * <p>
     * Öffnet das Stats-Popup bzw. schließt es, falls es bereits offen ist.
     *
     * @param actionEvent Event
     */
    @FXML
    private void onStatsButtonClicked(ActionEvent actionEvent) {
        if (statsOpen) {
            closeStatsPanel();
        } else {
            openStatsPanel();
        }
    }

    /**
     * Wird aufgerufen, wenn der Schließen-Button im Stats-Popup geklickt wird.
     *
     * @param actionEvent Event
     */
    @FXML
    private void onCloseStatsClicked(ActionEvent actionEvent) {
        closeStatsPanel();
    }

    /**
     * Wird aufgerufen, wenn neben das Stats-Popup (auf den abgedunkelten Hintergrund) geklickt wird.
     *
     * @param event MouseEvent
     */
    @FXML
    private void onStatsOverlayClicked(MouseEvent event) {
        closeStatsPanel();
    }

    /**
     * Öffnet das Stats-Popup mit einer Animation von unten
     * und fordert die Match-Statistiken beim Server an.
     */
    private void openStatsPanel() {
        statsOpen = true;
        statsButton.getStyleClass().add("statsButtonActive");

        statsSummaryLabel.setText("Lade Spiele...");
        matchListBox.getChildren().clear();
        statsScrollPane.setVvalue(0);
        sendGetMatchesStatisticsRequest();

        if (!statsPanel.isVisible()) {
            statsPanel.setTranslateY(getStatsPanelHeight());
            statsOverlay.setOpacity(0);
            statsPanel.setVisible(true);
            statsOverlay.setVisible(true);
        }
        playStatsAnimation(true);
    }

    /**
     * Schließt das Stats-Popup mit einer Animation nach unten.
     */
    private void closeStatsPanel() {
        if (!statsOpen) {
            return;
        }
        statsOpen = false;
        statsButton.getStyleClass().remove("statsButtonActive");
        playStatsAnimation(false);
    }

    /**
     * Spielt die Ein- bzw. Ausblendanimation des Stats-Popups ab.
     * Eine eventuell noch laufende Animation wird vorher gestoppt.
     *
     * @param open true zum Einblenden, false zum Ausblenden
     */
    private void playStatsAnimation(boolean open) {
        if (statsTransition != null) {
            statsTransition.stop();
        }

        TranslateTransition slide = new TranslateTransition(STATS_ANIMATION_DURATION, statsPanel);
        slide.setToY(open ? 0 : getStatsPanelHeight());
        slide.setInterpolator(open ? Interpolator.EASE_OUT : Interpolator.EASE_IN);

        FadeTransition fade = new FadeTransition(STATS_ANIMATION_DURATION, statsOverlay);
        fade.setToValue(open ? 1 : 0);

        statsTransition = new ParallelTransition(slide, fade);
        if (!open) {
            statsTransition.setOnFinished(event -> {
                statsPanel.setVisible(false);
                statsOverlay.setVisible(false);
            });
        }
        statsTransition.play();
    }

    /**
     * Gibt die Höhe des Stats-Popups zurück, um die es zum Ausblenden nach unten verschoben wird.
     *
     * @return Höhe des Popups
     */
    private double getStatsPanelHeight() {
        return Math.max(statsPanel.getHeight(), statsPanel.prefHeight(-1));
    }

    /**
     * Sendet eine Anfrage an den Server, um die Statistiken der Matches des
     * angemeldeten Benutzers abzurufen.
     */
    private void sendGetMatchesStatisticsRequest() {
        client.getConn().requestMatchesStatistics();
    }

    /**
     * Verarbeitet die Antwort des Servers auf die Anfrage nach den
     * Match-Statistiken und zeigt die Spiele im Stats-Popup an.
     *
     * @param msg die Serverantwort mit den Match-Ergebnissen und Profilbildern
     */
    public void matchesStatisticsResponse(MatchesStatisticsResponse msg) {
        ArrayList<MatchResult> matchResults = msg.matches() != null ? msg.matches() : new ArrayList<>();
        HashMap<Integer, Image> profileImages = loadProfileImages(msg.profileImages());
        Platform.runLater(() -> showMatchResults(matchResults, profileImages));
    }

    /**
     * Wandelt die vom Server geschickten Profilbilder in Images um.
     * Bilder, die nicht gelesen werden können, werden ausgelassen.
     *
     * @param imageData Profilbilder nach User-ID
     * @return Images nach User-ID
     */
    private HashMap<Integer, Image> loadProfileImages(HashMap<Integer, byte[]> imageData) {
        HashMap<Integer, Image> images = new HashMap<>();
        if (imageData == null) {
            return images;
        }

        imageData.forEach((userId, data) -> {
            if (data != null) {
                Image image = new Image(new ByteArrayInputStream(data));
                if (!image.isError()) {
                    images.put(userId, image);
                }
            }
        });
        return images;
    }

    /**
     * Gibt das Standard-Profilbild für Spieler ohne eigenes Bild zurück.
     *
     * @return Standard-Profilbild
     */
    private Image getDefaultProfileImage() {
        if (defaultProfileImage == null) {
            defaultProfileImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/htl/steyr/uno/img/profile.png")),
                    64, 64, true, true
            );
        }
        return defaultProfileImage;
    }

    /**
     * Befüllt die Liste im Stats-Popup mit den übergebenen Matches.
     * Die Matches werden in der Reihenfolge des Servers angezeigt (neuestes zuerst).
     *
     * @param matchResults die Match-Ergebnisse des Benutzers
     * @param profileImages Profilbilder der Spieler nach User-ID
     */
    private void showMatchResults(ArrayList<MatchResult> matchResults, Map<Integer, Image> profileImages) {
        int userId = getClient().getConn().getUser().getId();
        matchListBox.getChildren().clear();

        if (matchResults.isEmpty()) {
            statsSummaryLabel.setText("Noch keine Spiele gespielt");
            Label emptyLabel = new Label("Spiel eine Runde, um hier deine Ergebnisse zu sehen.");
            emptyLabel.getStyleClass().add("subtitle");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setMinHeight(statsScrollPane.getPrefViewportHeight());
            emptyLabel.setAlignment(Pos.CENTER);
            matchListBox.getChildren().add(emptyLabel);
            return;
        }

        int wins = 0;
        for (MatchResult matchResult : matchResults) {
            int placement = getPlacement(matchResult, userId);
            if (placement == 1) {
                wins++;
            }
            matchListBox.getChildren().add(createMatchRow(matchResult, placement, userId, profileImages));
        }

        statsSummaryLabel.setText(matchResults.size() + (matchResults.size() == 1 ? " Spiel" : " Spiele")
                + " · " + wins + (wins == 1 ? " Sieg" : " Siege"));
        statsScrollPane.setVvalue(0);
    }

    /**
     * Ermittelt die Platzierung des Benutzers in einem Match.
     *
     * @param matchResult das Match
     * @param userId die ID des Benutzers
     * @return Platzierung (1 = Sieg) oder 0, falls der Benutzer nicht gefunden wurde
     */
    private int getPlacement(MatchResult matchResult, int userId) {
        for (MatchPlayer player : getPlayers(matchResult)) {
            if (player.userId() == userId) {
                return player.placement();
            }
        }
        return 0;
    }

    /**
     * Gibt die Spieler eines Matches zurück (nie null).
     *
     * @param matchResult das Match
     * @return die Spieler, sortiert nach Platzierung
     */
    private List<MatchPlayer> getPlayers(MatchResult matchResult) {
        return matchResult.getPlayers() != null ? matchResult.getPlayers() : List.of();
    }

    /**
     * Erstellt eine Zeile für ein Match im Stats-Popup.
     * <p>
     * Oben stehen Ergebnis und Datum, darunter alle Spieler des Matches
     * mit Platzierung, Profilbild und Namen.
     *
     * @param matchResult das Match
     * @param placement die Platzierung des Benutzers in diesem Match
     * @param userId die ID des angemeldeten Benutzers
     * @param profileImages Profilbilder der Spieler nach User-ID
     * @return die fertige Zeile
     */
    private HBox createMatchRow(MatchResult matchResult, int placement, int userId, Map<Integer, Image> profileImages) {
        boolean won = placement == 1;
        List<MatchPlayer> players = getPlayers(matchResult);
        int playerCount = players.size();

        Label placeBadge = new Label(placement > 0 ? String.valueOf(placement) : "–");
        placeBadge.getStyleClass().add("placeBadge");

        String title;
        if (won) {
            title = "Sieg";
        } else if (placement > 0) {
            title = "Platz " + placement + " von " + playerCount;
        } else {
            title = "Ergebnis unbekannt";
        }
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("matchTitle");

        Label dateLabel = new Label(matchResult.getTimestamp() != null
                ? MATCH_DATE_FORMAT.format(matchResult.getTimestamp().toLocalDateTime())
                : "Unbekanntes Datum");
        dateLabel.getStyleClass().add("subtitle");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(8, titleLabel, headerSpacer, dateLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox playersBox = new HBox(14);
        playersBox.setAlignment(Pos.CENTER_LEFT);
        for (MatchPlayer player : players) {
            playersBox.getChildren().add(createPlayerChip(player, player.userId() == userId, profileImages));
        }

        VBox content = new VBox(6, header, playersBox);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox row = new HBox(12, placeBadge, content);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("matchRow");

        if (won) {
            row.getStyleClass().add("matchRowWin");
            placeBadge.getStyleClass().add("placeBadgeWin");
        }
        return row;
    }

    /**
     * Erstellt die Anzeige eines Spielers innerhalb einer Match-Zeile:
     * Platzierung, rundes Profilbild und Name.
     *
     * @param player der Spieler
     * @param isOwnPlayer true, wenn es sich um den angemeldeten Benutzer handelt
     * @param profileImages Profilbilder der Spieler nach User-ID
     * @return die fertige Spieler-Anzeige
     */
    private HBox createPlayerChip(MatchPlayer player, boolean isOwnPlayer, Map<Integer, Image> profileImages) {
        Label placeLabel = new Label(player.placement() + ".");
        placeLabel.getStyleClass().add("matchPlayerPlace");

        ImageView avatar = new ImageView(profileImages.getOrDefault(player.userId(), getDefaultProfileImage()));
        avatar.setFitWidth(PLAYER_AVATAR_SIZE);
        avatar.setFitHeight(PLAYER_AVATAR_SIZE);
        avatar.setSmooth(true);
        avatar.setClip(new Circle(PLAYER_AVATAR_SIZE / 2, PLAYER_AVATAR_SIZE / 2, PLAYER_AVATAR_SIZE / 2));

        String username = player.username() != null ? player.username() : "Unbekannt";
        Label nameLabel = new Label(isOwnPlayer ? "Du" : username);
        nameLabel.getStyleClass().add(isOwnPlayer ? "matchPlayerSelf" : "matchPlayerName");
        nameLabel.setTooltip(new Tooltip(username));

        HBox chip = new HBox(5, placeLabel, avatar, nameLabel);
        chip.setAlignment(Pos.CENTER_LEFT);
        return chip;
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