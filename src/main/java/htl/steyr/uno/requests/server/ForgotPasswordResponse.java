package htl.steyr.uno.requests.server;

import java.io.Serializable;

/*
 * Status codes:
 * 0: Success - Code can be sent
 * 1: Already requested recently
 * 2: Wrong code
 * 3: Code correct, enter new password
 * 4: Password reset successful
 * 5: Something went wrong
 * 6: No account with this email
 */
public record ForgotPasswordResponse(Integer status) implements Serializable {

    @Override
    public String toString() {
        return "ForgotPasswordResponse{" +
                "status='" + status() +
                "'}";
    }
}