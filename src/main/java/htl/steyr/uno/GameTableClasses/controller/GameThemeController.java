package htl.steyr.uno.GameTableClasses.controller;

import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class GameThemeController {
    private static final String DEFAULT_THEME = "Classic";
    private static final String BLUE_SNOWFLAKE_PATH = "/htl/steyr/uno/img/events/snowflakeBlue.png";
    private static final String WHITE_SNOWFLAKE_PATH = "/htl/steyr/uno/img/events/snowflakeWhite.png";
    private static final String TUMBLEWEED_PATH = "/htl/steyr/uno/img/events/tumbleweed.png";

    private record Snowflake(ImageView view, double speed, double drift, double rotationSpeed) {
    }

    private final StackPane root;
    private String currentTheme = DEFAULT_THEME;
    private ImageView backgroundImageView;
    private Pane snowPane;
    private AnimationTimer snowfallTimer;
    private Timeline tumbleweedTimer;
    private ParallelTransition activeTumbleweed;
    private final List<Snowflake> snowflakes = new ArrayList<>();
    private long previousSnowFrame;
    private final Random snowRandom = new Random();
    private final Random tumbleweedRandom = new Random();
    private Image blueSnowflakeImage;
    private Image whiteSnowflakeImage;
    private Image tumbleweedImage;

    public GameThemeController(StackPane root) {
        this.root = root;
    }

    public void initializeBackground() {
        backgroundImageView = new ImageView();
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(0, backgroundImageView);

        snowPane = new Pane();
        snowPane.setPickOnBounds(false);
        snowPane.setMouseTransparent(true);
        snowPane.prefWidthProperty().bind(root.widthProperty());
        snowPane.prefHeightProperty().bind(root.heightProperty());
        root.getChildren().add(1, snowPane);
        switchTheme(DEFAULT_THEME);
    }

    public void installThemePicker() {
        ComboBox<String> themeDropdown = new ComboBox<>();
        themeDropdown.getItems().addAll("Desert", "Classic", "Aurora");
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
            currentTheme = newTheme;
            loadThemeStylesheet(newTheme);
            setSnowfallEnabled("Aurora".equals(newTheme));
            setTumbleweedEnabled("Desert".equals(newTheme));
            String imagePath = getBackgroundImagePath(newTheme);
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

    private String getBackgroundImagePath(String theme) {
        String basePath = "/htl/steyr/uno/img/table/" + theme.toLowerCase(Locale.ROOT);
        if ("Desert".equals(theme)) {
            var desertImage = getClass().getResource(basePath + ".png");
            if (desertImage != null) {
                return basePath + ".png";
            }
            desertImage = getClass().getResource(basePath + ".jpg");
            if (desertImage != null) {
                return basePath + ".jpg";
            }
            return "/htl/steyr/uno/img/table/western.jpg";
        }
        return basePath + ".jpg";
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

    private void setSnowfallEnabled(boolean enabled) {
        if (snowPane == null) {
            return;
        }

        if (!enabled) {
            if (snowfallTimer != null) {
                snowfallTimer.stop();
            }
            snowflakes.clear();
            snowPane.getChildren().clear();
            return;
        }

        if (snowfallTimer != null) {
            snowfallTimer.stop();
        }
        snowflakes.clear();
        snowPane.getChildren().clear();
        if (blueSnowflakeImage == null) {
            blueSnowflakeImage = loadEventImage(BLUE_SNOWFLAKE_PATH);
        }
        if (whiteSnowflakeImage == null) {
            whiteSnowflakeImage = loadEventImage(WHITE_SNOWFLAKE_PATH);
        }
        for (int i = 0; i < 75; i++) {
            createSnowflake();
        }

        previousSnowFrame = 0;
        snowfallTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (previousSnowFrame == 0) {
                    previousSnowFrame = now;
                    return;
                }

                double seconds = Math.min((now - previousSnowFrame) / 1_000_000_000.0, 0.05);
                previousSnowFrame = now;
                double width = snowPane.getWidth();
                double height = snowPane.getHeight();

                for (Snowflake snowflake : snowflakes) {
                    ImageView flake = snowflake.view();
                    flake.setTranslateY(flake.getTranslateY() + snowflake.speed() * seconds);
                    flake.setTranslateX(flake.getTranslateX()
                            + Math.sin(now / 1_400_000_000.0 + flake.getLayoutY()) * snowflake.drift() * seconds);
                    flake.setRotate(flake.getRotate() + snowflake.rotationSpeed() * seconds);

                    if (flake.getLayoutY() + flake.getTranslateY() > height) {
                        flake.setTranslateY(-flake.getFitHeight() - snowRandom.nextDouble() * height * 0.25);
                        flake.setLayoutX(snowRandom.nextDouble() * width);
                    }
                    double x = flake.getLayoutX() + flake.getTranslateX();
                    if (x > width) {
                        flake.setTranslateX(-flake.getLayoutX() - flake.getFitWidth());
                    } else if (x + flake.getFitWidth() < 0) {
                        flake.setTranslateX(width - flake.getLayoutX());
                    }
                }
            }
        };
        snowfallTimer.start();
    }

    private void createSnowflake() {
        Image image = snowRandom.nextBoolean() ? blueSnowflakeImage : whiteSnowflakeImage;
        if (image == null) {
            image = blueSnowflakeImage != null ? blueSnowflakeImage : whiteSnowflakeImage;
        }
        if (image == null) {
            return;
        }

        ImageView flake = new ImageView(image);
        double size = 14 + snowRandom.nextDouble() * 20;
        flake.setFitWidth(size);
        flake.setFitHeight(size);
        flake.setPreserveRatio(true);
        flake.setSmooth(true);
        flake.setOpacity(0.55 + snowRandom.nextDouble() * 0.4);
        flake.setLayoutX(snowRandom.nextDouble() * Math.max(root.getWidth(), 1));
        flake.setLayoutY(snowRandom.nextDouble() * Math.max(root.getHeight(), 1));
        flake.setTranslateY(-snowRandom.nextDouble() * 140);
        snowflakes.add(new Snowflake(flake, 28 + snowRandom.nextDouble() * 48,
                7 + snowRandom.nextDouble() * 15, -25 + snowRandom.nextDouble() * 50));
        snowPane.getChildren().add(flake);
    }

    private void setTumbleweedEnabled(boolean enabled) {
        if (tumbleweedTimer != null) {
            tumbleweedTimer.stop();
        }
        if (activeTumbleweed != null) {
            activeTumbleweed.stop();
            activeTumbleweed = null;
        }
        snowPane.getChildren().removeIf(node -> "tumbleweed".equals(node.getId()));

        if (!enabled) {
            return;
        }

        tumbleweedTimer = new Timeline(new KeyFrame(Duration.seconds(14 + tumbleweedRandom.nextDouble() * 12), event -> {
            if ("Desert".equals(currentTheme)) {
                rollTumbleweed();
            }
        }));
        tumbleweedTimer.setCycleCount(Timeline.INDEFINITE);
        tumbleweedTimer.play();
    }

    private void rollTumbleweed() {
        if (activeTumbleweed != null && activeTumbleweed.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            return;
        }
        if (tumbleweedImage == null) {
            tumbleweedImage = loadEventImage(TUMBLEWEED_PATH);
        }
        if (tumbleweedImage == null) {
            return;
        }

        ImageView tumbleweed = new ImageView(tumbleweedImage);
        tumbleweed.setId("tumbleweed");
        double size = 135 + tumbleweedRandom.nextDouble() * 35;
        double distance = root.getWidth() + size * 3;
        double travelTime = 7 + tumbleweedRandom.nextDouble() * 3;
        tumbleweed.setFitWidth(size);
        tumbleweed.setFitHeight(size);
        tumbleweed.setPreserveRatio(true);
        tumbleweed.setSmooth(true);
        tumbleweed.setOpacity(0.9);
        tumbleweed.setLayoutY(root.getHeight() - size * 0.78);
        tumbleweed.setLayoutX(-size * 1.5);
        snowPane.getChildren().add(tumbleweed);

        TranslateTransition roll = new TranslateTransition(Duration.seconds(travelTime), tumbleweed);
        roll.setByX(distance);
        roll.setInterpolator(Interpolator.LINEAR);
        roll.setOnFinished(event -> {
            snowPane.getChildren().remove(tumbleweed);
            activeTumbleweed = null;
        });

        RotateTransition spin = new RotateTransition(Duration.seconds(travelTime), tumbleweed);
        spin.setByAngle(distance / (Math.PI * size) * 360);
        spin.setInterpolator(Interpolator.LINEAR);

        activeTumbleweed = new ParallelTransition(roll, spin);
        activeTumbleweed.play();
    }

    private Image loadEventImage(String path) {
        var resource = getClass().getResource(path);
        if (resource == null) {
            System.err.println("Warnung: Event-Bild nicht gefunden: " + path);
            return null;
        }
        Image image = new Image(resource.toExternalForm());
        if (image.isError()) {
            System.err.println("Fehler beim Laden des Event-Bildes: " + path);
            return null;
        }
        return image;
    }
}
