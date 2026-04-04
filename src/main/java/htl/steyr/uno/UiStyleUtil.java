package htl.steyr.uno;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Objects;

public final class UiStyleUtil {
    private static final String GLOBAL_FOCUS_STYLESHEET = Objects.requireNonNull(
            UiStyleUtil.class.getResource("/htl/steyr/uno/style/globalFocus.css"),
            "globalFocus.css not found"
    ).toExternalForm();

    private UiStyleUtil() {
    }

    public static void applyGlobalFocusStyle(Scene scene) {
        Parent root = scene.getRoot();

        if (!root.getStylesheets().contains(GLOBAL_FOCUS_STYLESHEET)) {
            root.getStylesheets().add(GLOBAL_FOCUS_STYLESHEET);
        }

        root.setFocusTraversable(true);
        Platform.runLater(root::requestFocus);
    }

    public static void applyRoundedCardClip(ImageView imageView, double width, double height, double arcSize) {
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(arcSize);
        clip.setArcHeight(arcSize);
        imageView.setClip(clip);
    }

    /**
     * Setzt das AppIcon auf die übergebene Stage.
     * Das Icon wird aus der Ressource img/logo.png geladen.
     *
     * @param stage die Stage auf der das Icon gesetzt werden soll
     */
    public static void setAppIcon(Stage stage) {
        try {
            Image icon = new Image(Objects.requireNonNull(
                    UiStyleUtil.class.getResourceAsStream("/htl/steyr/uno/img/logo.png")
            ));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des AppIcons: " + e.getMessage());
        }
    }
}
