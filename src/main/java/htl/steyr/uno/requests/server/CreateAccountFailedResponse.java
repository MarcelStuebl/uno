package htl.steyr.uno.requests.server;

import java.io.Serializable;

public class CreateAccountFailedResponse implements Serializable {

    private Integer status;
    /*
     * Status codes:
     * 1: Username already exists
     * 2: Verification code is incorrect
     */



    /**
     * Create a new CreateAccountFailedResponse.
     */
    public CreateAccountFailedResponse(Integer status) {
        setStatus(status);
    }


    @Override
    public String toString() {
        return "CreateAccountFailedResponse{" +
                "status='" + status +
                "'}";
    }


    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getStatus() {
        return status;
    }


}
