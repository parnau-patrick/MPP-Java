package org.example.network.dto;

import java.io.Serializable;

/**
 * DTO for transferring user data over network
 */
public class UserDTO implements Serializable {
    private String username;
    private String password;
    private String officeName;

    public UserDTO() {}

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDTO(String username, String password, String officeName) {
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
}