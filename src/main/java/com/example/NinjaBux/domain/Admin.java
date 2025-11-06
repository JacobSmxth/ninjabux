package com.example.NinjaBux.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Must have username")
    @Column(unique = true)
    private String username;

    // i dont know if this is needed, but too scared to delete
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Must have password hash")
    private String passwordHash;

    private String firstName;
    private String lastName;

    private boolean canCreateAdmins = false;

    public Admin() {}

    public Admin(String username, String email, String passwordHash, String firstName, String lastName, boolean canCreateAdmins) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canCreateAdmins = canCreateAdmins;
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
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
