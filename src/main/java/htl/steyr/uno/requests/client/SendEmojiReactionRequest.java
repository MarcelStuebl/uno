package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record SendEmojiReactionRequest(String emoji) implements Serializable {
}
