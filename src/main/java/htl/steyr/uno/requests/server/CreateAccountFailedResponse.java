package htl.steyr.uno.requests.server;

import java.io.Serializable;

/*
 * Status codes:
 * 1: Username already exists
 * 2: Verification code is incorrect
 */
public record CreateAccountFailedResponse(Integer status) implements Serializable {

    @Override
    public String toString() {
        return "CreateAccountFailedResponse{" +
                "status='" + status() +
                "'}";
    }
}