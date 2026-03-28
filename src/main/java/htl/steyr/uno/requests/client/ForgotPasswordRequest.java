package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record ForgotPasswordRequest(String email) implements Serializable {

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "email='" + email() +
                "'}";
    }
}