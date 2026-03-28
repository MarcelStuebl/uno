package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record ForgotPasswordSendCodeRequest(Integer code) implements Serializable {

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "email='" + code() +
                "'}";
    }
}