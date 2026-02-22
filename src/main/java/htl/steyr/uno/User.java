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


    /**
     * Constructor for the User class. Initializes all fields of the User object with the provided parameters.
     * This constructor is typically used when creating a User object from data retrieved from a database, where all fields are already known.
     * The parameters include the user's ID, username, last name, first name, number of games won and lost, timestamps for account creation and last login, and the password hash and salt for authentication purposes.
     * The constructor uses setter methods to assign values to the fields, which allows for any necessary validation or processing to be performed within the setter methods.
     *
     * @param id
     * @param username
     * @param lastName
     * @param firstName
     * @param gamesWon
     * @param gamesLost
     * @param createdAt
     * @param lastLogin
     * @param passwordHash
     * @param passwordSalt
     */
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


    /**
     * Constructor for the User class. Initializes all fields of the User object except for the password hash and salt.
     * This constructor is typically used when creating a User object from data retrieved from a database, where the password hash and salt are not included in the data.
     * The parameters include the user's ID, username, last name, first name, number of games won and lost, and timestamps for account creation and last login.
     * The constructor uses setter methods to assign values to the fields, which allows for any necessary validation or processing to be performed within the setter methods.
     *
     * @param id
     * @param username
     * @param lastName
     * @param firstName
     * @param gamesWon
     * @param gamesLost
     * @param createdAt
     * @param lastLogin
     */
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


    /**
     * Constructor for the User class. Initializes the username, last name, first name, and password fields of the User object.
     * This constructor is typically used when creating a new User object for registration or account creation purposes, where the user's ID, game statistics, and timestamps are not yet known.
     * The parameters include the username, last name, first name, and password for the new user. The password is processed to generate a password hash and salt for secure storage and authentication.
     * The constructor uses setter methods to assign values to the fields, which allows for any necessary validation or processing to be performed within the setter methods.
     *
     * @param username
     * @param lastName
     * @param firstName
     * @param Password
     */
    public User(String username, String lastName, String firstName, String Password) {
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
        generatePasswordHashAndSalt(Password);
    }


    /**
     * Constructor for the User class. Initializes the username, last name, and first name fields of the User object.
     * This constructor is typically used when creating a new User object for registration or account creation purposes, where the user's ID, game statistics, timestamps, and password are not yet known.
     * The parameters include the username, last name, and first name for the new user. The password is not included in this constructor, so it is assumed that the password will be set separately after the User object is created.
     * The constructor uses setter methods to assign values to the fields, which allows for any necessary validation or processing to be performed within the setter methods.
     *
     * @param username
     * @param lastName
     * @param firstName
     */
    public User(String username, String lastName, String firstName) {
        setUsername(username);
        setLastName(lastName);
        setFirstName(firstName);
    }


    /**
     * Default constructor for the User class. Initializes a new User object with default values for all fields.
     * This constructor is typically used when creating a User object without providing any initial data, such as when deserializing a User object from a file or network stream.
     * The fields of the User object will be set to their default values (e.g., null for strings, 0 for integers) until they are explicitly set using setter methods.
     */
    public User(){
    }


    /**
     * Generates a password hash and salt for the given password. This method uses a utility class (PasswordUtil) to generate a random salt and hash the password using the salt.
     * The generated salt and hash are then stored in the User object's passwordSalt and passwordHash fields, respectively.
     * This method is typically called when setting a new password for a user, such as during registration or password change processes, to ensure that the password is securely stored and can be verified during authentication.
     *
     * @param password The plaintext password for which the hash and salt will be generated.
     */
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




