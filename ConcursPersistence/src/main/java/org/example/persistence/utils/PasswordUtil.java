package org.example.persistence.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for password encryption and verification using jBCrypt
 */
public class PasswordUtil {

    /**
     * Encrypt a password using BCrypt
     * @param plainPassword Plain text password
     * @return Encrypted password hash
     */
    public static String encryptPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify if a plain password matches an encrypted password
     * @param plainPassword Plain text password
     * @param hashedPassword Encrypted password hash
     * @return True if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}