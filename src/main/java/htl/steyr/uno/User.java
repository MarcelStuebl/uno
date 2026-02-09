package htl.steyr.uno;

public class User {

    private int id;
    private String username;
    private String lastName;
    private String firstName;

    public User(int id, String username, String lastName, String firstName) {
        setId(id);
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


}




