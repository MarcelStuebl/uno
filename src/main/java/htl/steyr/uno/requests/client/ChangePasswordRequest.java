package htl.steyr.uno.requests.client;

import java.io.Serializable;

public record ChangePasswordRequest(String email, Integer code, String newPassword) implements Serializable {

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "code='" + code() +
                "'}";
    }
}