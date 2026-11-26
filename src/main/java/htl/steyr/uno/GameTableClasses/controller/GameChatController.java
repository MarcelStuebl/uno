package htl.steyr.uno.GameTableClasses.controller;

import htl.steyr.uno.client.Client;
import htl.steyr.uno.requests.server.ReceiveChatMessageResponse;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public final class GameChatController {
    private final Client client;
    private final StackPane root;
    private final StackPane overlayLayer;

    private StackPane chatBubbleButton;
    private Label unreadBadge;
    private VBox chatPanel;
    private ListView<ChatMessage> chatListView;
    private TextField chatInput;
    private boolean chatOpen;
    private int unreadMessageCount;
    private boolean keepingChatOnTop;

    public GameChatController(Client client, StackPane root, StackPane overlayLayer) {
        this.client = client;
        this.root = root;
        this.overlayLayer = overlayLayer;
    }

    public void install(Stage stage) {
        chatBubbleButton = createChatBubble();
        overlayLayer.getChildren().add(chatBubbleButton);
        chatPanel = createChatPanel();
        overlayLayer.getChildren().add(chatPanel);

        root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!chatOpen) {
                return;
            }

            Object target = event.getTarget();
            if (target instanceof Node node && isInsideChatInteraction(node)) {
                return;
            }

            closeChatPanel();
        });

        root.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!chatOpen || chatListView == null
                    || !chatListView.localToScene(chatListView.getBoundsInLocal())
                    .contains(event.getSceneX(), event.getSceneY())) {
                return;
            }

            Optional<ScrollBar> verticalScrollBar = chatListView.lookupAll(".scroll-bar").stream()
                    .filter(ScrollBar.class::isInstance)
                    .map(ScrollBar.class::cast)
                    .filter(scrollBar -> scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL)
                    .findFirst();
            if (verticalScrollBar.isPresent()) {
                int scrollSteps = (int) Math.max(1, Math.abs(event.getDeltaY()) / 40);
                ScrollBar scrollBar = verticalScrollBar.get();
                for (int i = 0; i < scrollSteps; i++) {
                    if (event.getDeltaY() > 0) {
                        scrollBar.decrement();
                    } else if (event.getDeltaY() < 0) {
                        scrollBar.increment();
                    }
                }
            }
            event.consume();
        });

        Scene scene = stage.getScene();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.T && !event.isConsumed()) {
                if (chatInput != null && chatInput.isFocused()) {
                    return;
                }
                animateBubbleClick(chatBubbleButton);
                toggleChatPanel();
            }
        });

        root.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> keepChatOnTop());
        overlayLayer.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> keepChatOnTop());
    }

    public void receiveMessage(ReceiveChatMessageResponse message) {
        if (chatListView == null || message == null || message.user() == null) {
            return;
        }

        Platform.runLater(() -> {
            chatListView.getItems().add(new ChatMessage(message.user().getUsername(), message.message()));
            if (!chatOpen) {
                chatListView.scrollTo(chatListView.getItems().size() - 1);
                unreadMessageCount++;
                updateUnreadBadge();
            }
        });
    }

    private StackPane createChatBubble() {
        Label chatIcon = new Label("💬");
        chatIcon.setStyle("-fx-font-size: 22px;");

        Label keyHint = new Label("[T]");
        keyHint.getStyleClass().add("game-chat-key");

        VBox bubbleContent = new VBox(2, chatIcon, keyHint);
        bubbleContent.setAlignment(Pos.CENTER);

        unreadBadge = new Label();
        unreadBadge.getStyleClass().add("game-chat-unread-badge");
        unreadBadge.setMinSize(22, 22);
        unreadBadge.setPrefSize(22, 22);
        unreadBadge.setMaxSize(22, 22);
        unreadBadge.setAlignment(Pos.CENTER);
        unreadBadge.setVisible(false);
        unreadBadge.setManaged(false);

        StackPane bubble = new StackPane(bubbleContent, unreadBadge);
        bubble.setPrefSize(76, 76);
        bubble.setMaxSize(76, 76);
        bubble.getStyleClass().add("game-chat-bubble");
        StackPane.setAlignment(unreadBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(unreadBadge, new Insets(2, 2, 0, 0));
        bubble.setOnMouseClicked(event -> {
            animateBubbleClick(bubble);
            toggleChatPanel();
        });

        StackPane.setAlignment(bubble, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(bubble, new Insets(0, -8, 18, 0));
        return bubble;
    }

    private VBox createChatPanel() {
        chatListView = new ListView<>();
        chatListView.getStyleClass().add("game-chat-list");
        chatListView.setPrefSize(320, 260);
        chatListView.setMaxSize(320, 260);
        chatListView.setMinSize(320, 260);
        chatListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String currentUser = client.getConn() != null && client.getConn().getUser() != null
                        ? client.getConn().getUser().getUsername() : "";
                boolean mine = currentUser.equals(item.sender());

                Label senderLabel = new Label(item.sender());
                senderLabel.getStyleClass().add("game-chat-sender");
                Label bubble = new Label(item.text());
                bubble.setWrapText(true);
                bubble.setMaxWidth(220);
                bubble.getStyleClass().add(mine ? "game-chat-bubble-mine" : "game-chat-bubble-other");

                VBox body = new VBox(3, senderLabel, bubble);
                body.setFillWidth(false);
                body.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                HBox row = new HBox(body);
                row.setPadding(new Insets(4, 8, 4, 8));
                row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });

        chatInput = new TextField();
        chatInput.setPromptText("Nachricht senden...");
        chatInput.getStyleClass().add("game-chat-input");
        chatInput.setOnAction(event -> sendCurrentMessage());

        VBox panel = new VBox(8, chatListView, chatInput);
        panel.setPadding(new Insets(10));
        panel.setPrefSize(340, 320);
        panel.setMaxSize(340, 320);
        panel.getStyleClass().add("game-chat-panel");
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setOpacity(0.0);
        StackPane.setAlignment(panel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(panel, new Insets(0, -20, 90, 0));
        return panel;
    }

    private boolean isInsideChatInteraction(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == chatPanel || current == chatBubbleButton || current == chatInput || current == chatListView) {
                return true;
            }
        }
        return false;
    }

    private void toggleChatPanel() {
        if (chatPanel == null) {
            return;
        }
        if (chatOpen) {
            closeChatPanel();
            return;
        }

        chatOpen = true;
        unreadMessageCount = 0;
        updateUnreadBadge();
        chatPanel.setVisible(true);
        chatPanel.setManaged(true);
        keepChatOnTop();

        FadeTransition fade = new FadeTransition(Duration.millis(150), chatPanel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setOnFinished(event -> chatInput.requestFocus());
        fade.play();
    }

    private void keepChatOnTop() {
        if (!chatOpen || keepingChatOnTop || root == null || overlayLayer == null || chatPanel == null) {
            return;
        }

        keepingChatOnTop = true;
        try {
            if (!root.getChildren().isEmpty()
                    && root.getChildren().get(root.getChildren().size() - 1) != overlayLayer) {
                overlayLayer.toFront();
            }
            if (!overlayLayer.getChildren().isEmpty()
                    && overlayLayer.getChildren().get(overlayLayer.getChildren().size() - 1) != chatPanel) {
                chatPanel.toFront();
            }
        } finally {
            keepingChatOnTop = false;
        }
    }

    private void closeChatPanel() {
        if (chatPanel == null || !chatOpen) {
            return;
        }

        chatOpen = false;
        FadeTransition fade = new FadeTransition(Duration.millis(120), chatPanel);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(event -> {
            chatPanel.setVisible(false);
            chatPanel.setManaged(false);
        });
        fade.play();
    }

    private void sendCurrentMessage() {
        String text = chatInput.getText();
        if (text == null || text.isBlank()) {
            return;
        }

        String trimmed = text.trim();
        chatInput.clear();
        if (client.getConn() != null && client.getConn().getUser() != null) {
            client.sendChatMessage(trimmed);
        }
    }

    private void updateUnreadBadge() {
        if (unreadBadge == null) {
            return;
        }
        unreadBadge.setText(unreadMessageCount > 9 ? "9+" : Integer.toString(unreadMessageCount));
        boolean hasUnreadMessages = unreadMessageCount > 0;
        unreadBadge.setVisible(hasUnreadMessages);
        unreadBadge.setManaged(hasUnreadMessages);
    }

    private void animateBubbleClick(Node node) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(80), node);
        scaleDown.setToX(0.85);
        scaleDown.setToY(0.85);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(80), node);
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        new SequentialTransition(scaleDown, scaleUp).play();
    }

    private record ChatMessage(String sender, String text) {
    }
}
