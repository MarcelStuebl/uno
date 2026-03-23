package htl.steyr.uno;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

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
}

