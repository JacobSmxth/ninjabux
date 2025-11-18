package com.example.NinjaBux.dto;

public class NinjaLoginRequest {
    private String username;

    public NinjaLoginRequest() {
    }

    public NinjaLoginRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
