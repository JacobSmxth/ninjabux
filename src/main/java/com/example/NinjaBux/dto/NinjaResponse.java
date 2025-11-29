package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.domain.enums.BeltPath;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NinjaResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private int buxBalance;
    private int legacyBalance;
    private int totalBuxEarned;
    private int totalBuxSpent;
    private int legacyPoints;
    private BeltType currentBeltType;
    private BeltPath beltPath;
    private int currentLevel;
    private int currentLesson;
    @JsonProperty("isLocked")
    private boolean locked;
    private LocalDateTime createdAt;
    private LocalDateTime lastProgressUpdate;

    public NinjaResponse() {}

    public NinjaResponse(Ninja ninja, int buxBalance, int legacyBalance) {
        this.id = ninja.getId();
        this.firstName = ninja.getFirstName();
        this.lastName = ninja.getLastName();
        this.username = ninja.getUsername();
        this.buxBalance = buxBalance;
        this.legacyBalance = legacyBalance;
        this.legacyPoints = ninja.getLegacyPoints();
        this.totalBuxEarned = 0;
        this.totalBuxSpent = 0;
        this.currentBeltType = ninja.getCurrentBeltType();
        this.beltPath = ninja.getBeltPath();
        this.currentLevel = ninja.getCurrentLevel();
        this.currentLesson = ninja.getCurrentLesson();
        this.locked = ninja.isLocked();
        this.createdAt = ninja.getCreatedAt();
        this.lastProgressUpdate = ninja.getLastProgressUpdate();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public int getBuxBalance() {
        return buxBalance;
    }
    public void setBuxBalance(int buxBalance) {
        this.buxBalance = buxBalance;
    }

    public int getLegacyBalance() {
        return legacyBalance;
    }
    public void setLegacyBalance(int legacyBalance) {
        this.legacyBalance = legacyBalance;
    }
    public int getTotalBuxEarned() {
        return totalBuxEarned;
    }
    public void setTotalBuxEarned(int totalBuxEarned) {
        this.totalBuxEarned = totalBuxEarned;
    }

    public int getTotalBuxSpent() {
        return totalBuxSpent;
    }
    public void setTotalBuxSpent(int totalBuxSpent) {
        this.totalBuxSpent = totalBuxSpent;
    }

    public int getLegacyPoints() {
        return legacyPoints;
    }
    public void setLegacyPoints(int legacyPoints) {
        this.legacyPoints = legacyPoints;
    }

    public BeltType getCurrentBeltType() {
        return currentBeltType;
    }
    public void setCurrentBeltType(BeltType currentBeltType) {
        this.currentBeltType = currentBeltType;
    }
    public BeltPath getBeltPath() { return beltPath; }
    public void setBeltPath(BeltPath beltPath) { this.beltPath = beltPath; }

    public int getCurrentLevel() {
        return currentLevel;
    }
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getCurrentLesson() {
        return currentLesson;
    }
    public void setCurrentLesson(int currentLesson) {
        this.currentLesson = currentLesson;
    }

    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastProgressUpdate() {
        return lastProgressUpdate;
    }

    public void setLastProgressUpdate(LocalDateTime lastProgressUpdate) {
        this.lastProgressUpdate = lastProgressUpdate;
    }
}
