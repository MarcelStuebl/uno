package htl.steyr.uno.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * The wire format used by every UNO WebSocket client:
 *
 * <pre>
 * {"type":"LoginRequest","payload":{"username":"alice","password":"..."}}
 * </pre>
 *
 * Message type names are the public simple names of the existing request and
 * response classes. The payload deliberately remains a normal JSON object so
 * browser and Android clients do not need Java serialization support.
 */
public final class JsonMessageCodec {
    public static final int PROTOCOL_VERSION = 1;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonMessageCodec() {
    }

    public static String encode(Object message) throws IOException {
        ObjectNode envelope = MAPPER.createObjectNode();
        envelope.put("version", PROTOCOL_VERSION);
        envelope.put("type", message.getClass().getSimpleName());
        envelope.set("payload", MAPPER.valueToTree(message));
        return MAPPER.writeValueAsString(envelope);
    }

    public static DecodedMessage decode(String json) throws IOException {
        JsonNode envelope = MAPPER.readTree(json);
        if (envelope == null || !envelope.isObject()
                || !envelope.has("version") || !envelope.get("version").canConvertToInt()
                || envelope.get("version").asInt() != PROTOCOL_VERSION
                || !envelope.hasNonNull("type") || !envelope.get("type").isTextual()
                || !envelope.has("payload")) {
            throw new IOException("Unsupported or invalid UNO message envelope");
        }
        return new DecodedMessage(envelope.get("type").asText(), envelope.get("payload"));
    }

    public static <T> T convert(DecodedMessage message, Class<T> type) throws IOException {
        return MAPPER.treeToValue(message.payload(), type);
    }

    public record DecodedMessage(String type, JsonNode payload) {
    }
}
