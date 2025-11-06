package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.Admin;

public class AdminResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean canCreateAdmins;

    public AdminResponse() {}

    public AdminResponse(Admin admin) {
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.email = admin.getEmail();
        this.firstName = admin.getFirstName();
        this.lastName = admin.getLastName();
        this.canCreateAdmins = admin.isCanCreateAdmins();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
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

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isCanCreateAdmins() {
        return canCreateAdmins;
    }
    public void setCanCreateAdmins(boolean canCreateAdmins) {
        this.canCreateAdmins = canCreateAdmins;
    }
}
