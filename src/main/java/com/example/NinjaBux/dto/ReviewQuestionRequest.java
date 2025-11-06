package com.example.NinjaBux.dto;

public class ReviewQuestionRequest {
    private String adminUsername;
    private String reason; // For rejection

    public String getAdminUsername() {
        return adminUsername;
    }
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}

