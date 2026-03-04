package htl.steyr.uno.requests.client;

import java.io.Serializable;

public class ForgotPasswordSendCodeRequest implements Serializable {

    private Integer code;

    /**
     * Create a new ForgotPasswordRequest with the given code.
     *
     * @param code the authentication code that the user has received via email and is now submitting to verify their identity. This code is typically a numeric value that was generated and sent to the user's email address as part of the password reset process. The server will validate this code against the one it generated and sent to ensure that the user is authorized to reset their password.
     */
    public ForgotPasswordSendCodeRequest(Integer code) {
        setCode(code);
    }

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "email='" + getCode() +
                "'}";
    }

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

}
