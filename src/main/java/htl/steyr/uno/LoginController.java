package htl.steyr.uno;

import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller-Klasse für den Login-, Registrierungs- und Passwort-Reset-Bereich.
 * <p>
 * Diese Klasse verwaltet:
 * <ul>
 *     <li>Login-Prozess</li>
 *     <li>Account-Erstellung</li>
 *     <li>2-Faktor-Authentifizierung (2FA)</li>
 *     <li>Passwort zurücksetzen</li>
 *     <li>Navigation zwischen verschiedenen Login-Screens</li>
 *     <li>Kommunikation mit dem Server über den Client</li>
 * </ul>
 */
public class LoginController implements Initializable {

    // UI-Elemente
    @FXML public Button anmeldeButton;
    @FXML public Label errorLabel;
    @FXML public Label errorLabelCreateAcc;
    @FXML public TextField newAccEmail;
    @FXML public TextField twoFACode;
    @FXML public VBox show2FA;
    @FXML public Label errorLabel2FA;
    @FXML public VBox showForgotPassword;
    @FXML public TextField resetPasswordEmail;
    @FXML public Label errorLabelForgotPassword;
    @FXML public VBox showResetPassword2FA;
    @FXML public TextField resetPassword2FACode;
    @FXML public Label errorLabelReset2FA;
    @FXML public VBox showNewPasswordScreen;
    @FXML public PasswordField newPassword;
    @FXML public PasswordField confirmNewPassword;
    @FXML public Label errorLabelNewPassword;
    @FXML public VBox showAccountVerificationScreen;
    @FXML public TextField verifyAccount;
    @FXML private Button showLogin;
    @FXML private VBox showNewAccScreen;
    @FXML private Button createAcc;
    @FXML private VBox showConnectionScreen;
    @FXML private VBox showLoginScreen;
    @FXML private StackPane loginPane;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private StackPane brandingPane;
    @FXML private PasswordField newAccPassword;
    @FXML private TextField newAccUserName;
    @FXML private TextField newAccLastName;
    @FXML private TextField newAccFirstName;
    @FXML private PasswordField welcomeBackPasswd;
    @FXML private TextField welcomeBackUserName;
    @FXML private HBox rootContainer;

    /** Client zur Serverkommunikation */
    private Client client;

    // Temporär gespeicherte Eingaben
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String code;

    /**
     * Initialisiert den Controller.
     * <p>
     * Startet den Client-Thread und zeigt einen Ladebildschirm,
     * bis die Verbindung zum Server bereit ist.
     *
     * @param url Ressourcen-URL
     * @param resourceBundle Ressourcen-Bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setConnectionLoading(true);

        client = new Client(this);
        Thread clientThread = new Thread(client::start);
        clientThread.start();

        Platform.runLater(() -> {
            rootContainer.requestFocus();
            loginPane.getScene().getWindow().setOnCloseRequest(event -> onSceneClose());
        });
    }

    /**
     * Wird aufgerufen, wenn die Verbindung bereit ist.
     */
    public void readyToLogin() {
        Platform.runLater(() -> setConnectionLoading(false));
    }

    /**
     * Wird aufgerufen, wenn keine Verbindung möglich ist.
     */
    public void cantLogin() {
        Platform.runLater(() -> setConnectionLoading(true));
    }

    /**
     * Aktiviert oder deaktiviert den Ladebildschirm.
     *
     * @param loading true = Ladeanzeige anzeigen
     */
    private void setConnectionLoading(boolean loading) {
        if (loading) {
            hideAllLoginSubScreens();
            showConnectionScreen.setVisible(true);
            showLoginScreen.setVisible(false);
        } else {
            showConnectionScreen.setVisible(false);
            showLoginScreen.setVisible(true);
        }

        if (loadingSpinner != null) {
            loadingSpinner.setVisible(loading);
        }
    }

    /**
     * Versteckt alle Unter-Screens des Login-Bereichs.
     */
    private void hideAllLoginSubScreens() {
        show2FA.setVisible(false);
        showNewAccScreen.setVisible(false);
        showForgotPassword.setVisible(false);
        showResetPassword2FA.setVisible(false);
        showNewPasswordScreen.setVisible(false);
        showAccountVerificationScreen.setVisible(false);
    }

    /**
     * Zeigt den Account-Erstellen-Screen an.
     */
    @FXML
    private void onCreateAccountButtonClicked(ActionEvent actionEvent) {
        showLoginScreen.setVisible(false);
        showNewAccScreen.setVisible(true);
    }

    /**
     * Setzt das Login-Fehlerlabel zurück.
     */
    private void resetLoginErrorLabel() {
        errorLabel.setVisible(false);
        errorLabel.setText("ERROR!!!");
    }

    /**
     * Navigiert zurück zum Login-Screen.
     */
    @FXML
    private void backToLogin() {
        showNewAccScreen.setVisible(false);
        showLoginScreen.setVisible(true);
        resetLoginErrorLabel();
    }

    /**
     * Verarbeitet die Erstellung eines neuen Accounts.
     */
    public void onCreateNewAccountButtonClicked(ActionEvent actionEvent) {
        username = newAccUserName.getText();
        password = newAccPassword.getText();
        firstName = newAccFirstName.getText();
        lastName = newAccLastName.getText();
        email = newAccEmail.getText();

        if (password.isEmpty()) {
            errorLabelCreateAcc.setText("Password cannot be empty!");
        } else if (firstName.isEmpty()) {
            errorLabelCreateAcc.setText("First name cannot be empty!");
        } else if (lastName.isEmpty()) {
            errorLabelCreateAcc.setText("Last name cannot be empty!");
        } else if (email.isEmpty()) {
            errorLabelCreateAcc.setText("Email cannot be empty!");
        } else {
            client.getConn().checkIfUserAlreadyExists(username, email);
        }

        errorLabelCreateAcc.setVisible(true);
    }

    /**
     * Verarbeitet die Serverantwort zur Account-Existenzprüfung.
     *
     * @param msg Serverantwort
     */
    public void checkIfUserAlreadyExistsResponse(CheckIfUserAlreadyExistsResponse msg) {
        if (msg.userAlreadyExists()) {
            errorLabelCreateAcc.setText("Username already exists!");
        } else if (msg.emailAlreadyExists()) {
            errorLabelCreateAcc.setText("Email already exists!");
        } else {
            client.createAccount(username, lastName, firstName, email, password);
        }
    }

    /**
     * Wird ausgelöst, wenn der Login-Button gedrückt wird.
     *
     * @param actionEvent Event
     */
    public void onLoginButtonClicked(ActionEvent actionEvent) {
        String username = welcomeBackUserName.getText();
        String password = welcomeBackPasswd.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Username cannot be empty!");
        } else if (password.isEmpty()) {
            errorLabel.setText("Password cannot be empty!");
        } else {
            client.logIn(username, password);
        }

        errorLabel.setVisible(true);
    }

    /**
     * Wird aufgerufen bei erfolgreichem Login.
     *
     * @param user eingeloggter Benutzer
     */
    public void logInSuccess(User user) {
        Platform.runLater(() -> {
            try {
                switchScene();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Wird aufgerufen bei fehlgeschlagenem Login.
     *
     * @param msg Fehlermeldung
     */
    public void logInFailed(LoginFailedResponse msg) {
        String errorMessage = switch (msg.errorCode()) {
            case 1 -> "Invalid username or password!";
            case 2 -> "User already logged in!";
            default -> "Unknown error!";
        };

        Platform.runLater(() -> {
            errorLabel.setText(errorMessage);
            errorLabel.setVisible(true);
        });
    }

    /**
     * Öffnet den "Passwort vergessen"-Screen.
     */
    @FXML
    private void onForgotPasswordClicked(ActionEvent actionEvent) {
        showLoginScreen.setVisible(false);
        showForgotPassword.setVisible(true);
    }

    /**
     * Wird beim Schließen des Fensters aufgerufen.
     */
    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    /**
     * Wechselt zur Lobby-Szene nach erfolgreichem Login.
     *
     * @throws IOException wenn FXML nicht geladen werden kann
     */
    public void switchScene() throws IOException {
        Stage stage = new Stage();
        Stage thisStage = (Stage) errorLabel.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("lobby.fxml"));
        LobbyController controller = new LobbyController(client);

        loader.setController(controller);
        controller.getClient().setLobbyController(controller);

        Scene scene = new Scene(loader.load());
        UiStyleUtil.applyGlobalFocusStyle(scene);

        stage.setTitle("UNO - Lobby");
        stage.setScene(scene);
        UiStyleUtil.setAppIcon(stage);
        stage.setMaximized(true);
        stage.show();

        thisStage.close();
    }

    /**
     * Gibt den Client zurück.
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
}