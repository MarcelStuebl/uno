package htl.steyr.uno;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {


    /**
     * Generates a random salt value for password hashing. The salt is a random sequence of bytes that is used to enhance the security of password hashes by adding uniqueness to each password hash, even if multiple users have the same password.
     * The method uses the SecureRandom class to generate a secure random byte array of length 16, which is then encoded to a Base64 string for storage and use in password hashing.
     * @return A Base64-encoded string representing the generated salt value.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }


    /**
     * Hashes a password using the SHA-256 algorithm and a provided salt value. The method takes the plaintext password and the salt as input, combines them, and then applies the SHA-256 hashing algorithm to produce a secure hash of the password.
     * The resulting hash is then encoded to a Base64 string for storage and comparison during authentication processes. This method is typically used to securely store user passwords in a database, allowing for verification of user credentials without storing the plaintext password.
     * @param password The plaintext password that needs to be hashed.
     * @param salt The salt value that will be combined with the password before hashing to enhance security.
     * @return A Base64-encoded string representing the hashed password.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            String saltedPassword = password + salt;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());

            return Base64.getEncoder().encodeToString(hashedBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}




