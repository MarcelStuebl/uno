package htl.steyr.uno.requests.server;

import java.io.Serializable;

public class LoginFailedResponse implements Serializable {

    private Integer errorCode;
    /**
     * Error code indicating the reason for the login failure. Possible values include:
     * 1: Invalid username or password.
     * 2: User already logged in from another device.
     */


    /**
     * Create a new LoginFailedResponse.
     */
    public LoginFailedResponse(Integer errorCode) {
        setErrorCode(errorCode);
    }


    @Override
    public String toString() {
        return "LoginFailedResponse{}";
    }


    public Integer getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }



}
