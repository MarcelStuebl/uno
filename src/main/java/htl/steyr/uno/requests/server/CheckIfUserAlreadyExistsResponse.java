package htl.steyr.uno.requests.server;

public class CheckIfUserAlreadyExistsResponse {

    private String username;
    private boolean userAlreadyExists = true;

    public CheckIfUserAlreadyExistsResponse(String username, boolean userAlreadyExists) {
        setUsername(username);
        setUserAlreadyExists(userAlreadyExists);
    }

    @Override
    public String toString() {
        return "CheckIfUserAlreadyExistsRequest{" +
                "username='" + getUsername() +
                '}';
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String userName) {
        this.username = username;
    }

    public boolean isUserAlreadyExists() {
        return userAlreadyExists;
    }
    public void setUserAlreadyExists(boolean userAlreadyExists) {
        this.userAlreadyExists = userAlreadyExists;
    }

}
