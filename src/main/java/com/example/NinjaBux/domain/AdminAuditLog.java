package com.example.NinjaBux.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminUsername;

    @Column(nullable = false)
    private String action;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Long targetNinjaId;
    private String targetNinjaName;

    public AdminAuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AdminAuditLog(String adminUsername, String action, String details) {
        this.adminUsername = adminUsername;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public AdminAuditLog(String adminUsername, String action, String details, Long targetNinjaId, String targetNinjaName) {
        this.adminUsername = adminUsername;
        this.action = action;
        this.details = details;
        this.targetNinjaId = targetNinjaId;
        this.targetNinjaName = targetNinjaName;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAdminUsername() {
        return adminUsername;
    }
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public Long getTargetNinjaId() {
        return targetNinjaId;
    }
    public void setTargetNinjaId(Long targetNinjaId) {
        this.targetNinjaId = targetNinjaId;
    }
    public String getTargetNinjaName() {
        return targetNinjaName;
    }
    public void setTargetNinjaName(String targetNinjaName) {
        this.targetNinjaName = targetNinjaName;
    }
}
