package com.example.NinjaBux.dto;

public class CreateAdminByAdminRequest {
    private String currentAdminUsername;
    private String currentAdminPassword;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    public CreateAdminByAdminRequest() {}

    public String getCurrentAdminUsername() {
        return currentAdminUsername;
    }
    public void setCurrentAdminUsername(String currentAdminUsername) {
        this.currentAdminUsername = currentAdminUsername;
    }

    public String getCurrentAdminPassword() {
        return currentAdminPassword;
    }
    public void setCurrentAdminPassword(String currentAdminPassword) {
        this.currentAdminPassword = currentAdminPassword;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

