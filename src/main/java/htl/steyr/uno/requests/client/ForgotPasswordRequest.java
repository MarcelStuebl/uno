package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class ForgotPasswordRequest implements Serializable {

    private String email;

    /**
     * Create a new ForgotPasswordRequest with the given email.
     *
     * @param email the email address associated with the account for which the password reset is requested
     */
    public ForgotPasswordRequest(String email) {
        setEmail(email);
    }

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "email='" + getEmail() +
                "'}";
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}
