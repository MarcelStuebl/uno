package htl.steyr.uno;

import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX-Controller für die Login-Oberfläche des UNO-Spiels.
 *
 * <p>Verwaltet den gesamten Authentifizierungsbereich der Anwendung. Dazu zählen
 * folgende Teilbereiche, die jeweils als eigener {@link VBox}-Screen realisiert sind:
 * <ul>
 *   <li><b>Login</b> – Anmeldung mit Benutzername und Passwort</li>
 *   <li><b>Registrierung</b> – Erstellen eines neuen Benutzerkontos</li>
 *   <li><b>Konto-Verifizierung</b> – Aktivierung des neuen Kontos per E-Mail-Code</li>
 *   <li><b>2FA beim Login</b> – Zwei-Faktor-Authentifizierung nach dem Login</li>
 *   <li><b>Passwort vergessen</b> – E-Mail-basierter Passwort-Reset-Ablauf</li>
 *   <li><b>Verbindungsanzeige</b> – Ladescreen während des Server-Verbindungsaufbaus</li>
 * </ul>
 *
 * <p>Die Kommunikation mit dem Server erfolgt über eine {@link Client}-Instanz,
 * die in einem eigenen Netzwerk-Thread läuft. Alle UI-Aktualisierungen werden
 * deshalb über {@link Platform#runLater(Runnable)} auf den JavaFX Application Thread
 * delegiert.
 *
 * <p>Nach erfolgreichem Login wechselt der Controller mittels {@link #switchScene()}
 * zur Lobby-Ansicht ({@code lobby.fxml}).
 *
 * @see Client
 * @see LobbyController
 */
public class LoginController implements Initializable {

    /** Schaltfläche zum Absenden des Login-Formulars. */
    @FXML public Button anmeldeButton;

    /** Fehlermeldungs-Label auf dem Haupt-Login-Screen. */
    @FXML public Label errorLabel;

    /** Fehlermeldungs-Label auf dem Registrierungs-Screen. */
    @FXML public Label errorLabelCreateAcc;

    /** Eingabefeld für die E-Mail-Adresse bei der Registrierung. */
    @FXML public TextField newAccEmail;

    /** Eingabefeld für den 2FA-Code beim Login. */
    @FXML public TextField twoFACode;

    /** Container für den 2FA-Screen beim Login. */
    @FXML public VBox show2FA;

    /** Fehlermeldungs-Label auf dem 2FA-Login-Screen. */
    @FXML public Label errorLabel2FA;

    /** Container für den "Passwort vergessen"-Screen. */
    @FXML public VBox showForgotPassword;

    /** Eingabefeld für die E-Mail-Adresse beim Passwort-Zurücksetzen. */
    @FXML public TextField resetPasswordEmail;

    /** Fehlermeldungs-Label auf dem "Passwort vergessen"-Screen. */
    @FXML public Label errorLabelForgotPassword;

    /** Container für den 2FA-Verifizierungsscreen beim Passwort-Reset. */
    @FXML public VBox showResetPassword2FA;

    /** Eingabefeld für den 2FA-Code beim Passwort-Reset. */
    @FXML public TextField resetPassword2FACode;

    /** Fehlermeldungs-Label auf dem 2FA-Screen beim Passwort-Reset. */
    @FXML public Label errorLabelReset2FA;

    /** Container für den Screen zur Eingabe des neuen Passworts. */
    @FXML public VBox showNewPasswordScreen;

    /** Eingabefeld für das neue Passwort. */
    @FXML public PasswordField newPassword;

    /** Bestätigungsfeld für das neue Passwort. */
    @FXML public PasswordField confirmNewPassword;

    /** Fehlermeldungs-Label auf dem Neues-Passwort-Screen. */
    @FXML public Label errorLabelNewPassword;

    /** Container für den Konto-Verifizierungsscreen nach der Registrierung. */
    @FXML public VBox showAccountVerificationScreen;

    /** Eingabefeld für den Verifizierungscode bei der Konto-Aktivierung. */
    @FXML public TextField verifyAccount;

    /** Schaltfläche zum Wechseln zurück zum Login-Screen. */
    @FXML private Button showLogin;

    /** Container für den Registrierungs-Screen. */
    @FXML private VBox showNewAccScreen;

    /** Schaltfläche zum Öffnen des Registrierungs-Screens. */
    @FXML private Button createAcc;

    /** Container für den Verbindungs-/Lade-Screen. */
    @FXML private VBox showConnectionScreen;

    /** Container für den Haupt-Login-Screen. */
    @FXML private VBox showLoginScreen;

    /** Haupt-Container des gesamten Login-Dialogs. */
    @FXML private StackPane loginPane;

    /** Ladeindikator, der während des Verbindungsaufbaus angezeigt wird. */
    @FXML private ProgressIndicator loadingSpinner;

    /** Branding-Bereich (Logo/Name) des Login-Dialogs. */
    @FXML private StackPane brandingPane;

    /** Passwortfeld für das Passwort des neuen Kontos. */
    @FXML private PasswordField newAccPassword;

    /** Eingabefeld für den Benutzernamen des neuen Kontos. */
    @FXML private TextField newAccUserName;

    /** Eingabefeld für den Nachnamen des neuen Kontos. */
    @FXML private TextField newAccLastName;

    /** Eingabefeld für den Vornamen des neuen Kontos. */
    @FXML private TextField newAccFirstName;

    /** Passwortfeld für das Passwort beim Login. */
    @FXML private PasswordField welcomeBackPasswd;

    /** Eingabefeld für den Benutzernamen beim Login. */
    @FXML private TextField welcomeBackUserName;

    /** Wurzel-Container der gesamten Login-Ansicht. */
    @FXML private HBox rootContainer;

    /** Der Client, der die Netzwerkkommunikation mit dem Server übernimmt. */
    private Client client;

    /**
     * Zwischengespeicherter Benutzername für mehrstufige Abläufe
     * (z.&nbsp;B. Registrierung, Passwort-Reset).
     */
    private String username;

    /**
     * Zwischengespeichertes Passwort für mehrstufige Abläufe
     * (z.&nbsp;B. Registrierung, Passwort-Reset).
     */
    private String password;

    /** Zwischengespeicherter Vorname für den Registrierungsablauf. */
    private String firstName;

    /** Zwischengespeicherter Nachname für den Registrierungsablauf. */
    private String lastName;

    /**
     * Zwischengespeicherte E-Mail-Adresse für mehrstufige Abläufe
     * (z.&nbsp;B. Registrierung, Passwort-Reset).
     */
    private String email;

    /**
     * Zwischengespeicherter 2FA-Code, der für den finalen Passwort-Reset-Schritt
     * (Setzen des neuen Passworts) benötigt wird.
     */
    private String code;

    /**
     * Wird vom JavaFX-Framework automatisch nach dem Laden der FXML-Datei aufgerufen.
     *
     * <p>Führt folgende Schritte durch:
     * <ol>
     *   <li>Zeigt den Verbindungs-/Ladescreen an.</li>
     *   <li>Erstellt eine neue {@link Client}-Instanz und startet sie in einem
     *       eigenen Hintergrund-Thread.</li>
     *   <li>Registriert nach dem nächsten JavaFX-Rendering-Zyklus den
     *       {@code OnCloseRequest}-Handler des Fensters, um die Verbindung beim
     *       Schließen sauber zu trennen.</li>
     * </ol>
     *
     * @param url            URL der geladenen FXML-Ressource (wird nicht verwendet)
     * @param resourceBundle ResourceBundle für Lokalisierung (wird nicht verwendet)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setConnectionLoading(true);

        client = new Client(this);
        Thread clientThread = new Thread(() -> {
            client.start();
        });
        clientThread.start();

        Platform.runLater(() -> {
            rootContainer.requestFocus();
            loginPane.getScene().getWindow().setOnCloseRequest(event -> {
                onSceneClose();
            });
        });
    }

    /**
     * Wird vom {@link Client} aufgerufen, sobald die Verbindung zum Server
     * erfolgreich hergestellt wurde.
     *
     * <p>Blendet den Verbindungs-/Ladescreen aus und zeigt den Login-Screen an.
     * Der Aufruf erfolgt aus dem Netzwerk-Thread; die UI-Aktualisierung wird
     * daher über {@link Platform#runLater(Runnable)} auf den JavaFX Application
     * Thread delegiert.
     */
    public void readyToLogin() {
        Platform.runLater(() -> setConnectionLoading(false));
    }

    /**
     * Wird vom {@link Client} aufgerufen, wenn die Verbindung zum Server
     * fehlgeschlagen ist oder unterbrochen wurde.
     *
     * <p>Zeigt erneut den Verbindungs-/Ladescreen an, um dem Benutzer zu
     * signalisieren, dass aktuell keine Verbindung zum Server besteht.
     * Analog zu {@link #readyToLogin()} wird die UI-Aktualisierung über
     * {@link Platform#runLater(Runnable)} durchgeführt.
     */
    public void cantLogin() {
        Platform.runLater(() -> setConnectionLoading(true));
    }

    /**
     * Schaltet zwischen dem Verbindungs-/Ladescreen und dem Login-Screen um.
     *
     * <p>Im Lademodus werden alle Teilscreens ausgeblendet, der Verbindungsscreen
     * in den Vordergrund gebracht und der {@link ProgressIndicator} sichtbar gemacht.
     * Im Login-Modus geschieht das Gegenteil: Der Verbindungsscreen verschwindet und
     * der Login-Screen wird angezeigt.
     *
     * @param loading {@code true} zeigt den Verbindungsscreen mit Ladeindikator;
     *                {@code false} zeigt den eigentlichen Login-Screen
     */
    private void setConnectionLoading(boolean loading) {
        if (loading) {
            hideAllLoginSubScreens();
            showConnectionScreen.setVisible(true);
            showConnectionScreen.setManaged(true);
            showConnectionScreen.toFront();
            showLoginScreen.setVisible(false);
            showLoginScreen.setManaged(false);
        } else {
            showConnectionScreen.setVisible(false);
            showConnectionScreen.setManaged(false);
            showLoginScreen.setVisible(true);
            showLoginScreen.setManaged(true);
            showLoginScreen.toFront();
        }

        if (loadingSpinner != null) {
            loadingSpinner.setVisible(loading);
            loadingSpinner.setManaged(loading);
        }
    }

    /**
     * Blendet alle untergeordneten Login-Screens aus.
     *
     * <p>Wird intern aufgerufen, bevor ein bestimmter Teilscreen sichtbar gemacht
     * wird, um zu verhindern, dass mehrere Screens gleichzeitig sichtbar sind.
     * Betroffen sind: 2FA-Screen, Registrierungs-Screen, Passwort-vergessen-Screen,
     * Passwort-Reset-2FA-Screen, Neues-Passwort-Screen und Konto-Verifizierungsscreen.
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
     * Wechselt vom Login-Screen zum Registrierungs-Screen.
     *
     * <p>Leert die Login-Eingabefelder ({@code welcomeBackUserName},
     * {@code welcomeBackPasswd} und {@code newAccEmail}) und blendet alle
     * Fehlermeldungen aus, um einen sauberen Zustand herzustellen.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    @FXML
    private void onCreateAccountButtonClicked(ActionEvent actionEvent) {
        showLoginScreen.setVisible(false);
        showNewAccScreen.setVisible(true);
        welcomeBackPasswd.clear();
        welcomeBackUserName.clear();
        newAccEmail.clear();
        errorLabel.setVisible(false);
        errorLabelCreateAcc.setVisible(false);
    }

    /**
     * Setzt das Fehlerlabel des Login-Screens auf seinen Standardzustand zurück.
     *
     * <p>Das Label wird unsichtbar gemacht, der Text auf {@code "ERROR!!!"} und
     * die Textfarbe auf Rot ({@code #ff5a5a}) zurückgesetzt. Dies stellt sicher,
     * dass nach einer Erfolgsanzeige (z.&nbsp;B. grüner Text) wieder der
     * Standardzustand gilt.
     */
    private void resetLoginErrorLabel() {
        errorLabel.setVisible(false);
        errorLabel.setText("ERROR!!!");
        errorLabel.setStyle("-fx-text-fill: #ff5a5a;");
    }

    /**
     * Navigiert vom Registrierungs-Screen zurück zum Login-Screen.
     *
     * <p>Leert alle Registrierungsfelder ({@code newAccPassword}, {@code newAccUserName},
     * {@code newAccLastName}, {@code newAccFirstName}), setzt das Login-Fehlerlabel
     * zurück und blendet das Registrierungs-Fehlerlabel aus.
     */
    @FXML
    private void backToLogin() {
        showNewAccScreen.setVisible(false);
        showLoginScreen.setVisible(true);
        newAccPassword.clear();
        newAccUserName.clear();
        newAccLastName.clear();
        newAccFirstName.clear();
        resetLoginErrorLabel();
        errorLabelCreateAcc.setVisible(false);
    }

    /**
     * Navigiert vom 2FA-Screen (Login) zurück zum Login-Screen.
     *
     * <p>Leert das 2FA-Eingabefeld ({@code twoFACode}) und setzt das
     * Login-Fehlerlabel zurück.
     */
    @FXML
    private void backToLoginFrom2FA() {
        show2FA.setVisible(false);
        showLoginScreen.setVisible(true);
        twoFACode.clear();
        resetLoginErrorLabel();
    }

    /**
     * Verarbeitet die Eingabe des 2FA-Codes im Login-Ablauf.
     *
     * <p>Prüft, ob der Code nicht leer ist, und leert das Eingabefeld nach der
     * Eingabe. Die eigentliche Serveranfrage zur Verifizierung des Codes ist
     * noch nicht implementiert.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    @FXML
    private void onVerify2FA(ActionEvent actionEvent) {
        String code = twoFACode.getText();
        if (code.isEmpty()) {

        } else {
            twoFACode.clear();
        }
    }

    /**
     * Validiert die Eingaben im Registrierungsformular und sendet bei Erfolg
     * eine Anfrage an den Server, um die Eindeutigkeit von Benutzername und
     * E-Mail zu prüfen.
     *
     * <p>Die Felder werden in folgender Reihenfolge validiert:
     * <ol>
     *   <li>Passwort darf nicht leer sein.</li>
     *   <li>Vorname darf nicht leer sein.</li>
     *   <li>Nachname darf nicht leer sein.</li>
     *   <li>E-Mail darf nicht leer sein.</li>
     *   <li>Benutzername darf nur Kleinbuchstaben enthalten (Regex: {@code [a-z]+}).</li>
     *   <li>Vorname darf keine Ziffern und Nachname keine Sonderzeichen
     *       ({@code !@#$%^&*()_/}) enthalten.</li>
     *   <li>Nachname darf weder Ziffern noch Sonderzeichen enthalten.</li>
     *   <li>E-Mail muss mindestens {@code @} und {@code .} enthalten.</li>
     * </ol>
     *
     * <p>Schlägt eine Validierung fehl, wird {@code errorLabelCreateAcc} mit einer
     * entsprechenden Meldung sichtbar gemacht und kein Serverkontakt hergestellt.
     * Sind alle Felder gültig, wird
     * {@link htl.steyr.uno.client(String, String)}
     * aufgerufen; das Ergebnis wird asynchron über
     * {@link #checkIfUserAlreadyExistsResponse(CheckIfUserAlreadyExistsResponse)}
     * zurückgeliefert.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    public void onCreateNewAccountButtonClicked(ActionEvent actionEvent) {
        username = newAccUserName.getText();
        password = newAccPassword.getText();
        firstName = newAccFirstName.getText();
        lastName = newAccLastName.getText();
        email = newAccEmail.getText();

        if (password.isEmpty()) {
            errorLabelCreateAcc.setText("Password cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (firstName.isEmpty()) {
            errorLabelCreateAcc.setText("First name cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (lastName.isEmpty()) {
            errorLabelCreateAcc.setText("Last name cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (email.isEmpty()) {
            errorLabelCreateAcc.setText("Email cannot be empty!");
            errorLabelCreateAcc.setVisible(true);
        } else if (!(username.matches("[a-z]+"))) {
            errorLabelCreateAcc.setText("Username must only contain lowercase letters!");
            errorLabelCreateAcc.setVisible(true);
        } else if (firstName.matches(".*\\d.*") || lastName.matches(".*[!@#$%^&*()_/].*")) {
            errorLabelCreateAcc.setText("First name cannot contain numbers or special characters!");
            errorLabelCreateAcc.setVisible(true);
        } else if (lastName.matches(".*\\d.*") || lastName.matches(".*[!@#$%^&*()_/].*")) {
            errorLabelCreateAcc.setText("Last name cannot contain numbers or special characters!");
            errorLabelCreateAcc.setVisible(true);
        } else if (!(email.contains("@") && email.contains("."))) {
            errorLabelCreateAcc.setText("Email must be valid!");
            errorLabelCreateAcc.setVisible(true);
        } else {
            client.getConn().checkIfUserAlreadyExists(username, email);
        }
    }

    /**
     * Callback-Methode, die nach der serverseitigen Eindeutigkeitsprüfung von
     * Benutzername und E-Mail aufgerufen wird.
     *
     * <p>Ist bereits ein Konflikt vorhanden ({@code userAlreadyExists()} oder
     * {@code emailAlreadyExists()} ist {@code true}), wird eine entsprechende
     * Fehlermeldung im Registrierungs-Screen angezeigt.
     * Andernfalls wird das Konto über {@link Client#createAccount(String, String, String, String, String)}
     * angelegt und der Konto-Verifizierungsscreen eingeblendet.
     *
     * <p>Dieser Callback wird aus dem Netzwerk-Thread aufgerufen; alle
     * UI-Aktualisierungen erfolgen daher über {@link Platform#runLater(Runnable)}.
     *
     * @param msg die Serverantwort mit den Flags {@code userAlreadyExists()} und
     *            {@code emailAlreadyExists()}
     */
    public void checkIfUserAlreadyExistsResponse(CheckIfUserAlreadyExistsResponse msg) {
        if (msg.userAlreadyExists()) {
            Platform.runLater(() -> {
                errorLabelCreateAcc.setText("Username already exists!");
                errorLabelCreateAcc.setVisible(true);
            });
        } else if (msg.emailAlreadyExists()) {
            Platform.runLater(() -> {
                errorLabelCreateAcc.setText("Email already exists!");
                errorLabelCreateAcc.setVisible(true);
            });
        } else {
            client.createAccount(username, lastName, firstName, email, password);
            Platform.runLater(() -> {
                errorLabelCreateAcc.setVisible(false);
                showAccountVerificationScreen.setVisible(true);
                showNewAccScreen.setVisible(false);
            });
        }
    }

    /**
     * Verarbeitet den vom Benutzer eingegebenen Konto-Verifizierungscode nach
     * der Registrierung.
     *
     * <p>Der Code muss genau 6 Ziffern lang sein ({@code \\d+}). Bei gültigem Code
     * wird {@link Client#(String, String, String, String, String, int)}
     * aufgerufen, der Verifizierungsscreen ausgeblendet und der Login-Screen
     * eingeblendet. Zusätzlich wird für 2 Sekunden eine grüne Erfolgsmeldung
     * ({@code "2FA Code Authentifizierung erfolgreich! ✓"}) im Login-Fehlerlabel
     * angezeigt, bevor dieses wieder ausgeblendet und auf den Standardstil
     * zurückgesetzt wird.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    public void onVerifyNewAccount(ActionEvent actionEvent) {
        String code = verifyAccount.getText();
        if (code.isEmpty()) {
        } else if (code.length() != 6 || !code.matches("\\d+")) {
        } else {
            client.verifyNewAccount(username, firstName, lastName, email, password, Integer.parseInt(code));

            verifyAccount.clear();
            showAccountVerificationScreen.setVisible(false);
            showLoginScreen.setVisible(true);

            errorLabel.setText("2FA Code Authentifizierung erfolgreich! ✓");
            errorLabel.setStyle("-fx-text-fill: #90EE90;");
            errorLabel.setVisible(true);

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        errorLabel.setVisible(false);
                        errorLabel.setStyle("-fx-text-fill: #FF0000;");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Verarbeitet den Login-Versuch des Benutzers.
     *
     * <p>Prüft, ob Benutzername und Passwort nicht leer sind. Bei fehlenden
     * Eingaben wird das entsprechende Fehlerlabel sichtbar gemacht.
     * Sind beide Felder befüllt, wird {@link Client#logIn(String, String)}
     * aufgerufen und das Fehlerlabel ausgeblendet.
     *
     * <p>Das Ergebnis wird asynchron über {@link #logInSuccess(User)} oder
     * {@link #logInFailed(LoginFailedResponse)} zurückgeliefert.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     * @throws IOException wird weitergeleitet, falls {@link #switchScene()} beim
     *                     Laden der Lobby-FXML eine E/A-Ausnahme wirft
     */
    public void onLoginButtonClicked(ActionEvent actionEvent) throws IOException {
        String username = welcomeBackUserName.getText();
        String password = welcomeBackPasswd.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Username cannot be empty!");
            errorLabel.setVisible(true);
        } else if (password.isEmpty()) {
            errorLabel.setText("Password cannot be empty!");
            errorLabel.setVisible(true);
        } else {
            client.logIn(username, password);
            errorLabel.setVisible(false);
        }
    }

    /**
     * Wird vom {@link Client} aufgerufen, wenn das Konto erfolgreich auf dem
     * Server angelegt wurde.
     *
     * <p>Navigiert über {@link #backToLogin()} zurück zum Login-Screen. Der Aufruf
     * erfolgt aus dem Netzwerk-Thread; die Navigation wird daher über
     * {@link Platform#runLater(Runnable)} ausgeführt.
     *
     * @param msg die Serverantwort (Typ {@link CreateAccountSuccessResponse},
     *            Inhalt wird nicht ausgewertet)
     */
    public void createAccountSuccess(CreateAccountSuccessResponse msg) {
        Platform.runLater(this::backToLogin);
    }

    /**
     * Wird vom {@link Client} aufgerufen, wenn die Kontoerstellung auf dem
     * Server fehlgeschlagen ist.
     *
     * <p>Ausgewertete Statuscodes:
     * <ul>
     *   <li>{@code 1} – Benutzername bereits vergeben</li>
     *   <li>{@code 2} – Verifizierungscode ist falsch</li>
     * </ul>
     *
     * <p>Alle UI-Aktualisierungen werden über {@link Platform#runLater(Runnable)}
     * auf den JavaFX Application Thread delegiert.
     *
     * @param msg die Serverantwort mit dem Statuscode
     */
    public void createAccountFailedResponse(CreateAccountFailedResponse msg) {
        if (msg.status() == 1) {
            Platform.runLater(() -> {
                errorLabelCreateAcc.setText("Username already exists!");
                errorLabelCreateAcc.setVisible(true);
            });
        } else if (msg.status() == 2) {
            Platform.runLater(() -> {
                errorLabelCreateAcc.setText("Verification code is incorrect!");
                errorLabelCreateAcc.setVisible(true);
            });
        }

        // @TODO: Check if this is right
    }

    /**
     * Wird vom {@link Client} aufgerufen, wenn der Login erfolgreich war.
     *
     * <p>Wechselt über {@link #switchScene()} zur Lobby-Ansicht. Da der Aufruf
     * aus dem Netzwerk-Thread erfolgt, wird die Szenenwechsel-Logik über
     * {@link Platform#runLater(Runnable)} auf den JavaFX Application Thread
     * delegiert. Eine {@link IOException} beim Laden der Lobby-FXML wird als
     * {@link RuntimeException} weitergeworfen.
     *
     * @param user das {@link User}-Objekt mit den Sitzungsdaten des
     *             angemeldeten Benutzers
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
     * Wird vom {@link Client} aufgerufen, wenn der Login-Versuch fehlgeschlagen ist.
     *
     * <p>Ausgewertete Fehlercodes:
     * <ul>
     *   <li>{@code 1} – Falscher Benutzername oder falsches Passwort</li>
     *   <li>{@code 2} – Benutzer ist bereits von einem anderen Gerät eingeloggt</li>
     *   <li>Sonstiger Wert – Unbekannter Fehler</li>
     * </ul>
     *
     * <p>Die Fehlermeldung wird in {@code errorLabel} angezeigt. Die
     * UI-Aktualisierung erfolgt über {@link Platform#runLater(Runnable)}.
     *
     * @param msg die Serverantwort mit dem Fehlercode
     */
    public void logInFailed(LoginFailedResponse msg) {
        String errorMessage;
        if (msg.errorCode() == 1) {
            errorMessage = "Invalid username or password!";
        } else if (msg.errorCode() == 2) {
            errorMessage = "User already logged in from another device!";
        } else {
            errorMessage = "Unknown error!";
        }

        Platform.runLater(() -> {
            errorLabel.setText(errorMessage);
            errorLabel.setVisible(true);
        });
    }

    /**
     * Wechselt vom Login-Screen zum "Passwort vergessen"-Screen.
     *
     * <p>Leert {@code resetPasswordEmail} und blendet das Fehlerlabel aus,
     * um einen sauberen Ausgangszustand herzustellen.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    @FXML
    private void onForgotPasswordClicked(ActionEvent actionEvent) {
        showLoginScreen.setVisible(false);
        showForgotPassword.setVisible(true);
        resetPasswordEmail.clear();
        errorLabelForgotPassword.setVisible(false);
    }

    /**
     * Navigiert vom "Passwort vergessen"-Screen zurück zum Login-Screen.
     *
     * <p>Leert {@code resetPasswordEmail} und setzt das Login-Fehlerlabel zurück.
     */
    @FXML
    private void backToLoginFromForgotPassword() {
        showForgotPassword.setVisible(false);
        showLoginScreen.setVisible(true);
        resetPasswordEmail.clear();
        resetLoginErrorLabel();
    }

    /**
     * Sendet eine Passwort-Reset-Anfrage für die eingegebene E-Mail-Adresse
     * an den Server.
     *
     * <p>Validiert zunächst, dass die E-Mail nicht leer ist und mindestens
     * {@code @} sowie {@code .} enthält. Bei ungültiger Eingabe wird
     * {@code errorLabelForgotPassword} mit einer Fehlermeldung eingeblendet.
     * Bei gültiger Eingabe wird
     * {@link htl.steyr.uno.client(String)}
     * aufgerufen; das Ergebnis kommt asynchron über
     * {@link #forgotPasswordResponse(ForgotPasswordResponse)} zurück.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    @FXML
    private void onResetPasswordClicked(ActionEvent actionEvent) {
        email = resetPasswordEmail.getText();

        if (email.isEmpty()) {
            errorLabelForgotPassword.setText("Email cannot be empty!");
            errorLabelForgotPassword.setVisible(true);
        } else if (!(email.contains("@") && email.contains("."))) {
            errorLabelForgotPassword.setText("Email must be valid!");
            errorLabelForgotPassword.setVisible(true);
        } else {
            client.getConn().requestPasswordReset(email);
        }

        //@ToDo: Wenn email zu schnell hintereinander eingegeben wird, soll label zeigen "Try again later"
        // zu verwendendes label: errorLabelForgotPassword.setText("To fast! Try again Later");

    }

    /**
     * Callback-Methode für alle Serverantworten im mehrstufigen
     * Passwort-Reset-Ablauf.
     *
     * <p>Der Ablauf wird anhand des Statuscodes gesteuert:
     * <ul>
     *   <li>{@code 0} – E-Mail gefunden; blendet den Passwort-vergessen-Screen
     *       aus und zeigt den 2FA-Screen an.</li>
     *   <li>{@code 1} – Zu viele Anfragen in kurzer Zeit; zeigt die Meldung
     *       {@code "To fast! Try again later."} in {@code errorLabelForgotPassword}.</li>
     *   <li>{@code 2} – Eingegebener Code ist falsch; Fehlermeldung in der UI
     *       noch nicht implementiert ({@code @TODO}).</li>
     *   <li>{@code 3} – 2FA-Code korrekt; blendet den 2FA-Screen aus und zeigt
     *       den Neues-Passwort-Screen an.</li>
     *   <li>{@code 4} – Passwort erfolgreich geändert; zeigt eine grüne
     *       Erfolgsmeldung ({@code "Passwort erfolgreich geändert!"}) und
     *       navigiert nach 2 Sekunden automatisch zurück zum Login-Screen.</li>
     *   <li>{@code 5} – Allgemeiner Serverfehler; Fehlermeldung in der UI
     *       noch nicht implementiert ({@code @TODO}).</li>
     *   <li>{@code 6} – E-Mail-Adresse nicht gefunden; zeigt die Meldung
     *       {@code "Email address not found!"} in {@code errorLabelForgotPassword}.</li>
     * </ul>
     *
     * <p>Alle UI-Aktualisierungen werden über {@link Platform#runLater(Runnable)}
     * auf den JavaFX Application Thread delegiert.
     *
     * @param msg die Serverantwort mit dem Statuscode
     */
    public void forgotPasswordResponse(ForgotPasswordResponse msg) {
        if (msg.status() == 0) {
            showForgotPassword.setVisible(false);
            showResetPassword2FA.setVisible(true);
            resetPassword2FACode.clear();
            errorLabelReset2FA.setVisible(false);

        } else if (msg.status() == 1) {
            Platform.runLater(() -> {
                errorLabelForgotPassword.setText("To fast! Try again later.");
                errorLabelForgotPassword.setVisible(true);
            });
        } else if (msg.status() == 2) {
            System.out.println("Wrong code. Please try again.");


            // @TODO: Show error message.
        } else if (msg.status() == 3) {
            Platform.runLater(() -> {
                showResetPassword2FA.setVisible(false);
                showNewPasswordScreen.setVisible(true);
                newPassword.clear();
                confirmNewPassword.clear();
                errorLabelNewPassword.setVisible(false);
            });
        } else if (msg.status() == 4) {
            Platform.runLater(() -> {
                errorLabelNewPassword.setText("Passwort erfolgreich geändert!");
                errorLabelNewPassword.setStyle("-fx-text-fill: #90EE90;");
                errorLabelNewPassword.setVisible(true);
            });

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::backToLoginFromNewPassword);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (msg.status() == 5) {
            System.out.println("Something went wrong. Please try again later.");

            // @TODO: Show error message.
        } else if (msg.status() == 6) {
            Platform.runLater(() -> {
                errorLabelForgotPassword.setText("Email address not found!");
                errorLabelForgotPassword.setVisible(true);
            });
        }

    }

    /**
     * Verifiziert den 2FA-Code im Passwort-Reset-Ablauf.
     *
     * <p>Validiert, dass der Code nicht leer ist und genau 6 Ziffern enthält
     * (Regex: {@code \\d+}). Bei ungültiger Eingabe wird {@code errorLabelReset2FA}
     * mit einer Fehlermeldung eingeblendet. Bei gültigem Code wird
     * {@link htl.steyr.uno.client(int)}
     * aufgerufen; das Ergebnis kommt über {@link #forgotPasswordResponse(ForgotPasswordResponse)}
     * zurück (Statuscode {@code 2} bei falschem, {@code 3} bei richtigem Code).
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    @FXML
    private void onVerifyResetPassword2FA(ActionEvent actionEvent) {
        code = resetPassword2FACode.getText();

        if (code.isEmpty()) {
            errorLabelReset2FA.setText("Code cannot be empty!");
            errorLabelReset2FA.setVisible(true);
        } else if (code.length() != 6 || !code.matches("\\d+")) {
            errorLabelReset2FA.setText("Code must be 6 digits!");
            errorLabelReset2FA.setVisible(true);
        } else {
            client.getConn().verifyPasswordResetCode(Integer.parseInt(code));
        }
    }

    /**
     * Setzt das neue Passwort nach erfolgreicher 2FA-Verifizierung im
     * Passwort-Reset-Ablauf.
     *
     * <p>Validiert, dass das Passwortfeld nicht leer ist, das Bestätigungsfeld
     * nicht leer ist und beide Felder übereinstimmen. Bei Validierungsfehlern
     * wird {@code errorLabelNewPassword} eingeblendet. Bei erfolgreicher
     * Validierung wird
     * {@link htl.steyr.uno.client(String, int, String)}
     * mit der gespeicherten E-Mail-Adresse, dem gespeicherten 2FA-Code und dem
     * neuen Passwort aufgerufen.
     *
     * @param actionEvent das auslösende {@link ActionEvent} der Schaltfläche
     */
    @FXML
    private void onSetNewPasswordClicked(ActionEvent actionEvent) {
        String password = newPassword.getText();
        String confirmPassword = confirmNewPassword.getText();

        if (password.isEmpty()) {
            errorLabelNewPassword.setText("Password cannot be empty!");
            errorLabelNewPassword.setVisible(true);
        } else if (confirmPassword.isEmpty()) {
            errorLabelNewPassword.setText("Please confirm your password!");
            errorLabelNewPassword.setVisible(true);
        } else if (!password.equals(confirmPassword)) {
            errorLabelNewPassword.setText("Passwords do not match!");
            errorLabelNewPassword.setVisible(true);
        } else {
            client.getConn().setNewPassword(email, Integer.parseInt(code), password);
        }
    }

    /**
     * Navigiert vom 2FA-Reset-Screen zurück zum Login-Screen.
     *
     * <p>Blendet den Konto-Verifizierungsscreen, den Passwort-Reset-2FA-Screen
     * und den Passwort-vergessen-Screen aus, leert {@code resetPassword2FACode}
     * und setzt das Login-Fehlerlabel zurück.
     */
    @FXML
    private void backToLoginFromReset2FA() {
        showAccountVerificationScreen.setVisible(false);
        showResetPassword2FA.setVisible(false);
        showForgotPassword.setVisible(false);
        resetPassword2FACode.clear();
        showLoginScreen.setVisible(true);
        resetLoginErrorLabel();
    }

    /**
     * Navigiert vom Neues-Passwort-Screen zurück zum Login-Screen.
     *
     * <p>Blendet den Neues-Passwort-Screen aus, leert {@code newPassword},
     * {@code confirmNewPassword} und {@code resetPasswordEmail} und setzt
     * das Login-Fehlerlabel zurück.
     */
    @FXML
    private void backToLoginFromNewPassword() {
        showNewPasswordScreen.setVisible(false);
        showLoginScreen.setVisible(true);
        newPassword.clear();
        confirmNewPassword.clear();
        resetPasswordEmail.clear();
        resetLoginErrorLabel();
    }

    /**
     * Handler für das Schließen des Login-Fensters.
     *
     * <p>Trennt die bestehende Server-Verbindung sauber, indem
     * {@link htl.steyr.uno.client()} aufgerufen wird –
     * sofern eine Verbindung ({@code client.getConn() != null}) vorhanden ist.
     */
    private void onSceneClose() {
        if (client.getConn() != null) {
            client.getConn().close();
        }
    }

    /**
     * Wechselt nach erfolgreichem Login zur Lobby-Szene.
     *
     * <p>Führt folgende Schritte durch:
     * <ol>
     *   <li>Erstellt eine neue {@link Stage} für die Lobby.</li>
     *   <li>Lädt {@code lobby.fxml} über einen {@link FXMLLoader}.</li>
     *   <li>Erstellt einen {@link LobbyController} mit dem aktuellen {@link Client}
     *       und verknüpft ihn mit dem Loader sowie dem Client selbst.</li>
     *   <li>Wendet den globalen Fokus-Style via {@link UiStyleUtil#applyGlobalFocusStyle(Scene)}
     *       und das App-Icon via {@link UiStyleUtil#setAppIcon(Stage)} an.</li>
     *   <li>Zeigt die Lobby-Stage maximiert an und schließt das aktuelle
     *       Login-Fenster.</li>
     * </ol>
     *
     * @throws IOException wenn {@code lobby.fxml} nicht geladen werden kann
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
     * Gibt die aktuelle {@link Client}-Instanz zurück.
     *
     * @return der Client, der für die Netzwerkkommunikation mit dem Server
     *         zuständig ist
     */
    public Client getClient() {
        return client;
    }

    /**
     * Setzt die {@link Client}-Instanz.
     *
     * <p>Wird typischerweise nur in Testszenarien oder bei einer expliziten
     * Client-Ersetzung verwendet.
     *
     * @param client der zu verwendende Client; darf nicht {@code null} sein
     */
    public void setClient(Client client) {
        this.client = client;
    }

}