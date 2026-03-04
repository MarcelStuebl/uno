package htl.steyr.uno.requests.client;

public class ChangePasswordRequest {

    private String email;
    private Integer code;
    private String newPassword;

    public ChangePasswordRequest(String email, Integer code, String newPassword) {
        setEmail(email);
        setCode(code);
        setNewPassword(newPassword);
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "code='" + getCode() +
                "'}";
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
