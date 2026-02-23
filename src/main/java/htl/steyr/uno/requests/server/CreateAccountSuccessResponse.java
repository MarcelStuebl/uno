package htl.steyr.uno.requests.server;

import htl.steyr.uno.User;

import java.io.Serializable;

public class CreateAccountSuccessResponse implements Serializable {

    private final User user;
    /**
     * Create a new CreateAccountSuccessResponse.
     */
    public CreateAccountSuccessResponse(User user) {
        this.user = user;
    }


    @Override
    public String toString() {
        return "CreateAccountSuccessResponse{}";
    }

    public User getUser() {
        return user;
    }



}
