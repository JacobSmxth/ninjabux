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
    private double buxBalance;
    private int legacyBalance;
    private int totalBuxEarned;
    private int totalBuxSpent;
    private BeltType currentBeltType;
    private int currentLevel;
    private int currentLesson;
    private int totalQuestionsAnswered;
    private int totalQuestionsCorrect;
    private boolean suggestionsBanned;
    @JsonProperty("isLocked")
    private boolean isLocked;
    private String lockReason;
    private LocalDateTime lockedAt;
    private String adminNote;

    public NinjaResponse() {}

    public NinjaResponse(Ninja ninja, LedgerService ledgerService) {
        this.id = ninja.getId();
        this.firstName = ninja.getFirstName();
        this.lastName = ninja.getLastName();
        this.username = ninja.getUsername();
        
        // Use ledger service to get actual balances
        if (ledgerService != null) {
            // Round bux to whole numbers (no decimals)
            double rawBux = ledgerService.getBuxBalance(ninja.getId());
            this.buxBalance = Math.round(rawBux);
            this.legacyBalance = ledgerService.getLegacyBalance(ninja.getId());
        } else {
            // Fallback to deprecated fields if ledger service not available
            this.buxBalance = ninja.getBuxBalance();
            this.legacyBalance = 0;
        }
        
        // Keep deprecated fields for backwards compatibility (may be 0)
        this.totalBuxEarned = ninja.getTotalBuxEarned();
        this.totalBuxSpent = ninja.getTotalBuxSpent();
        this.currentBeltType = ninja.getCurrentBeltType();
        this.currentLevel = ninja.getCurrentLevel();
        this.currentLesson = ninja.getCurrentLesson();
        this.totalQuestionsAnswered = ninja.getTotalQuestionsAnswered();
        this.totalQuestionsCorrect = ninja.getTotalQuestionsCorrect();
        this.suggestionsBanned = ninja.isSuggestionsBanned();
        this.isLocked = ninja.isLocked();
        this.lockReason = ninja.getLockReason();
        this.lockedAt = ninja.getLockedAt();
        this.adminNote = ninja.getAdminNote();
    }
    
    // Legacy constructor for backwards compatibility (will use deprecated fields)
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

    public double getBuxBalance() {
        return buxBalance;
    }
    public void setBuxBalance(double buxBalance) {
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
        return isLocked;
    }
    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public String getLockReason() {
        return lockReason;
    }
    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}
