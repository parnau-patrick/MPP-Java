package org.example.domain;

/**
 * Represents a system user
 */
public class User extends Entity<Integer> {
    private String username;
    private String password;
    private String officeName;

    public User() {
        // Required for JavaFX property binding
    }

    public User(String username, String password, String officeName) {
        this.username = username;
        this.password = password;
        this.officeName = officeName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", officeName='" + officeName + '\'' +
                '}';
    }
}