package htl.steyr.uno.requests.server;

import java.io.Serializable;

public class ForgotPasswordResponse implements Serializable {

    private Integer status;

    /**
     * Create a new ForgotPasswordResponse with the given status.
     * The status code can represent various outcomes of the forgot password request, such as:
     * - 0: Success - You can send an authentication code. - User can be sent to enter the authentication code.
     * - 1: You have already requested a password reset recently. Please wait before trying again.
     * - 2: Wrong code.
     * - 3: Code correct, enter new password.
     * - 4: Password reset successful.
     * - 5: Something went wrong. Please try again later.
     * - 6: No account associated with this email address.
     *
     * @param status the status code representing the result of the forgot password request
     */
    public ForgotPasswordResponse(Integer status) {
        setStatus(status);
    }

    @Override
    public String toString() {
        return "ForgotPasswordResponse{" +
                "status='" + getStatus() +
                "'}";
    }

    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }



}
