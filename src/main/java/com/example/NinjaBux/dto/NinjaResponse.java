package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.service.LedgerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NinjaResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private int buxBalance;
    private int legacyBalance;
    private BeltType currentBeltType;
    private int currentLevel;
    private int currentLesson;
    private int totalQuestionsAnswered;
    private int totalQuestionsCorrect;
    private boolean suggestionsBanned;
    @JsonProperty("isLocked")
    private boolean locked;
    private LocalDateTime createdAt;
    private LocalDateTime lastProgressUpdate;

    public NinjaResponse() {}

    public NinjaResponse(Ninja ninja, LedgerService ledgerService) {
        this.id = ninja.getId();
        this.firstName = ninja.getFirstName();
        this.lastName = ninja.getLastName();
        this.username = ninja.getUsername();
        
        if (ledgerService != null) {
            this.buxBalance = ledgerService.getBuxBalance(ninja.getId());
            this.legacyBalance = ledgerService.getLegacyBalance(ninja.getId());
        }
        this.currentBeltType = ninja.getCurrentBeltType();
        this.currentLevel = ninja.getCurrentLevel();
        this.currentLesson = ninja.getCurrentLesson();
        this.totalQuestionsAnswered = ninja.getTotalQuestionsAnswered();
        this.totalQuestionsCorrect = ninja.getTotalQuestionsCorrect();
        this.suggestionsBanned = ninja.isSuggestionsBanned();
        this.locked = ninja.isLocked();
        this.createdAt = ninja.getCreatedAt();
        this.lastProgressUpdate = ninja.getLastProgressUpdate();
    }
    
    public NinjaResponse(Ninja ninja) {
        this(ninja, null);
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

    public BeltType getCurrentBeltType() {
        return currentBeltType;
    }
    public void setCurrentBeltType(BeltType currentBeltType) {
        this.currentBeltType = currentBeltType;
    }

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

    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }
    public void setTotalQuestionsAnswered(int totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }

    public int getTotalQuestionsCorrect() {
        return totalQuestionsCorrect;
    }
    public void setTotalQuestionsCorrect(int totalQuestionsCorrect) {
        this.totalQuestionsCorrect = totalQuestionsCorrect;
    }

    public boolean isSuggestionsBanned() {
        return suggestionsBanned;
    }
    public void setSuggestionsBanned(boolean suggestionsBanned) {
        this.suggestionsBanned = suggestionsBanned;
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
