package htl.steyr.uno.requests.server;

import java.io.Serializable;

/*
 * Error codes:
 * 1: Invalid username or password
 * 2: User already logged in from another device
 */
public record LoginFailedResponse(Integer errorCode) implements Serializable {

    @Override
    public String toString() {
        return "LoginFailedResponse{}";
    }
}