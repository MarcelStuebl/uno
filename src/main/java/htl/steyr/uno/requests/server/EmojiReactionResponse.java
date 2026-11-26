package htl.steyr.uno.requests.server;

import java.io.Serializable;

public record EmojiReactionResponse(String emoji, String senderUsername) implements Serializable {
    public EmojiReactionResponse(String emoji) {
        this(emoji, null);
    }
}
