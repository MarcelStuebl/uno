package htl.steyr.uno.GameTableClasses.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Locale;

public final class GameThemeController {
    private static final String DEFAULT_THEME = "Classic";

    private final StackPane root;
    private String currentTheme = DEFAULT_THEME;
    private ImageView backgroundImageView;

    public GameThemeController(StackPane root) {
        this.root = root;
    }

    public void initializeBackground() {
        backgroundImageView = new ImageView();
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(0, backgroundImageView);
        switchTheme(DEFAULT_THEME);
    }

    public void installThemePicker() {
        ComboBox<String> themeDropdown = new ComboBox<>();
        themeDropdown.getItems().addAll("Western", "Classic", "Aurora");
        themeDropdown.setValue(currentTheme);
        themeDropdown.getStyleClass().add("game-theme-picker");
        themeDropdown.setCellFactory(listView -> {
            listView.getStyleClass().add("game-theme-options");
            return new ListCell<>() {
                {
                    getStyleClass().add("game-theme-option");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
        });
        themeDropdown.setOnAction(event -> {
            String selectedTheme = themeDropdown.getValue();
            if (selectedTheme != null && !selectedTheme.equals(currentTheme)) {
                currentTheme = selectedTheme;
                switchTheme(selectedTheme);
            }
        });

        StackPane.setAlignment(themeDropdown, Pos.TOP_LEFT);
        StackPane.setMargin(themeDropdown, new Insets(10));
        root.getChildren().add(themeDropdown);
        themeDropdown.toFront();
    }

    private void switchTheme(String newTheme) {
        Platform.runLater(() -> {
            loadThemeStylesheet(newTheme);
            String imagePath = "/htl/steyr/uno/img/table/" + newTheme.toLowerCase(Locale.ROOT) + ".jpg";
            try {
                var resource = getClass().getResource(imagePath);
                if (resource != null) {
                    backgroundImageView.setImage(new Image(resource.toExternalForm()));
                } else {
                    System.err.println("Warnung: Bild-Datei nicht gefunden: " + imagePath);
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Laden des Themes: " + e.getMessage());
            }
        });
    }

    private void loadThemeStylesheet(String selectedTheme) {
        String themePath = "/htl/steyr/uno/style/GameTableThemes/GameTable" + selectedTheme + ".css";
        var themeResource = getClass().getResource(themePath);
        if (themeResource == null) {
            System.err.println("Warnung: Theme-Datei nicht gefunden: " + themePath);
            return;
        }
        root.getStylesheets().setAll(themeResource.toExternalForm());
    }
}
