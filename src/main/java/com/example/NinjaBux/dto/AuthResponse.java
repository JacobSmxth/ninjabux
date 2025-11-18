package com.example.NinjaBux.dto;

public class AuthResponse {
    private String token;
    private String type;
    private Long userId;
    private String username;

    public AuthResponse() {
    }

    public AuthResponse(String token, String type, Long userId, String username) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
