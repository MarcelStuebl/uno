package htl.steyr.uno;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {

    private int id;
    private String username;
    private String lastName;
    private String firstName;
    private int gamesWon;
    private int gamesLost;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private String passwordHash;
    private String passwordSalt;


    public User(int id, String username, String lastName, String firstName, int gamesWon, int gamesLost, Timestamp createdAt, Timestamp lastLogin, String passwordHash, String passwordSalt) {
        setId(id);
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
        setGamesWon(gamesWon);
        setGamesLost(gamesLost);
        setCreatedAt(createdAt);
        setLastLogin(lastLogin);
        setPasswordHash(passwordHash);
        setPasswordSalt(passwordSalt);
    }

    public User(int id, String username, String lastName, String firstName, int gamesWon, int gamesLost, Timestamp createdAt, Timestamp lastLogin) {
        setId(id);
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
        setGamesWon(gamesWon);
        setGamesLost(gamesLost);
        setCreatedAt(createdAt);
        setLastLogin(lastLogin);
    }

    public User(String username, String lastName, String firstName, String Password) {
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
        generatePasswordHashAndSalt(Password);
    }

    public User(String username, String lastName, String firstName) {
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
    }

    public User(){
    }

    private void generatePasswordHashAndSalt(String password) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        setPasswordSalt(salt);
        setPasswordHash(hash);
    }


    @Override
    public String toString() {
        String output = "";
        output += "ID: " + getId() + "\n";
        output += "Username: " + getUsername() + "\n";
        output += "Last Name: " + getLastName() + "\n";
        output += "First Name: " + getFirstName() + "\n";
        output += "Games Won: " + getGamesWon() + "\n";
        output += "Games Lost: " + getGamesLost() + "\n";
        output += "Created At: " + getCreatedAt() + "\n";
        output += "Last Login: " + getLastLogin() + "\n";
        output += "Password Hash: " + getPasswordHash() + "\n";
        output += "Password Salt: " + getPasswordSalt() + "\n";
        return output;
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

    public int getGamesWon() {
        return gamesWon;
    }
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }
    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}




