package htl.steyr.uno.requests.client;

import htl.steyr.uno.User;

import java.io.Serializable;

public class SetProfileImageRequest implements Serializable {

    private User user;
    private byte[] imageData;


    public SetProfileImageRequest(User user, byte[] imageData) {
        setUser(user);
        setImageData(imageData);
    }

    @Override
    public String toString() {
        return "SetProfileImageRequest{" +
                "username='" + user.getUsername() +
                "'}";
    }



    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public byte[] getImageData() {
        return imageData;
    }
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }


}
