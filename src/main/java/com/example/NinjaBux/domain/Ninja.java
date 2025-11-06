package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.BeltType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Ninja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Must have firstName")
    private String firstName;
    @NotBlank(message = "Must have lastName")
    private String lastName;

    @NotBlank(message = "Must have username")
    @Column(unique = true)
    private String username;

    /**
     * @deprecated Use LedgerService.getBuxBalance(ninjaId) instead. Kept for backwards compatibility.
     * Balance should be calculated from ledger entries.
     */
    @Deprecated
    @NotNull(message = "Must have totalBuxEarned")
    @Min(value = 0, message = "Can't have negative earned bux")
    private int totalBuxEarned;

    /**
     * @deprecated Use LedgerService.getBuxBalance(ninjaId) instead. Kept for backwards compatibility.
     * Balance should be calculated from ledger entries.
     */
    @Deprecated
    @NotNull(message = "Must have totalBuxSpent")
    @Min(value = 0, message = "Can't have negative spent bux")
    private int totalBuxSpent;

    private int currentLevel;
    private int currentLesson;

    @Enumerated(EnumType.STRING)
    private BeltType currentBeltType;

    private LocalDateTime createdAt;
    private LocalDateTime lastProgressUpdate;

    // quiz statistics
    private int totalQuestionsAnswered = 0;
    private int totalQuestionsCorrect = 0;

    // realized might have to add this because of how some may act
    private boolean suggestionsBanned = false;

    // Ledger-based currency fields
    /**
     * Total lessons completed across all time (for statistics)
     */
    private int lessonsAllTime = 0;

    /**
     * Lessons completed since last Legacy conversion (0-9, loops every 10)
     */
    private int lessonsSinceConversion = 0;

    /**
     * Lessons completed after onboarding/import (drives earning scaling)
     */
    private int postOnboardLessonCount = 0;

    /**
     * For belts with .5 rates (e.g., Green 2.5, Yellow 1.5), alternates between low/high integer payouts.
     * false = pay low value next (e.g., 2 for Green, 1 for Yellow)
     * true = pay high value next (e.g., 3 for Green, 2 for Yellow)
     */
    private boolean postLegacyAlternator = false;

    /**
     * Cached Bux balance (in quarters). Updated when ledger entries are written.
     * Can be recalculated from ledger if needed.
     */
    private Integer cachedBuxBalanceQuarters = null;

    /**
     * Cached Legacy balance (in whole Legacy units). Updated when legacy ledger entries are written.
     * Can be recalculated from legacy ledger if needed.
     */
    private Integer cachedLegacyBalance = null;

    // Lock fields
    private boolean isLocked = false;
    private String lockReason;
    private LocalDateTime lockedAt;

    // Admin note field for internal notes about the ninja
    @Column(length = 1000)
    private String adminNote;

    public Ninja() {
        this.createdAt = LocalDateTime.now();
        this.lastProgressUpdate = LocalDateTime.now();
        this.totalQuestionsAnswered = 0;
        this.totalQuestionsCorrect = 0;
        this.suggestionsBanned = false;
        this.lessonsAllTime = 0;
        this.lessonsSinceConversion = 0;
        this.postOnboardLessonCount = 0;
        this.postLegacyAlternator = false;
        this.isLocked = false;
    }

    public Ninja(String fName, String lName, String username, int earned, int spent, int cLesson, int cLevel, BeltType cBeltType) {
        this.firstName = fName;
        this.lastName = lName;
        this.username = username;
        this.totalBuxEarned = earned;
        this.totalBuxSpent = spent;
        this.currentLesson = cLesson;
        this.currentLevel = cLevel;
        this.currentBeltType = cBeltType;
        this.createdAt = LocalDateTime.now();
        this.lastProgressUpdate = LocalDateTime.now();
        this.totalQuestionsAnswered = 0;
        this.totalQuestionsCorrect = 0;
        this.lessonsAllTime = 0;
        this.lessonsSinceConversion = 0;
        this.postOnboardLessonCount = 0;
        this.postLegacyAlternator = false;
        this.isLocked = false;
    }



    public Long getId() {return id;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public String getUsername() {return username;}
    public int getTotalBuxEarned() {return totalBuxEarned;}
    public int getTotalBuxSpent() {return totalBuxSpent;}
    public int getBuxBalance() {return totalBuxEarned - totalBuxSpent;}
    public int getCurrentLevel() {return currentLevel;}
    public int getCurrentLesson() {return currentLesson;}
    public BeltType getCurrentBeltType() {return currentBeltType;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public LocalDateTime getLastProgressUpdate() {return lastProgressUpdate;}
    public int getTotalQuestionsAnswered() {return totalQuestionsAnswered;}
    public int getTotalQuestionsCorrect() {return totalQuestionsCorrect;}
    public boolean isSuggestionsBanned() {return suggestionsBanned;}

    public void setId(Long id) {this.id = id;}
    public void setFirstName(String firstname) {this.firstName = firstname;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public void setUsername(String username) {this.username = username;}
    public void setTotalBuxEarned(int totalBuxEarned) {this.totalBuxEarned = totalBuxEarned;}
    public void setTotalBuxSpent(int totalBuxSpent) {this.totalBuxSpent = totalBuxSpent;}
    public void setCurrentLevel(int currentLevel) {this.currentLevel = currentLevel;}
    public void setCurrentLesson(int currentLesson) {this.currentLesson = currentLesson;}
    public void setCurrentBeltType(BeltType currentBeltType) {this.currentBeltType = currentBeltType;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    public void setLastProgressUpdate(LocalDateTime lastProgressUpdate) {this.lastProgressUpdate = lastProgressUpdate;}
    public void setTotalQuestionsAnswered(int totalQuestionsAnswered) {this.totalQuestionsAnswered = totalQuestionsAnswered;}
    public void setTotalQuestionsCorrect(int totalQuestionsCorrect) {this.totalQuestionsCorrect = totalQuestionsCorrect;}
    public void setSuggestionsBanned(boolean suggestionsBanned) {this.suggestionsBanned = suggestionsBanned;}

    public void incrementQuestionsAnswered() {this.totalQuestionsAnswered++;}
    public void incrementQuestionsCorrect() {this.totalQuestionsCorrect++;}

    // New ledger-based fields getters and setters
    public int getLessonsAllTime() { return lessonsAllTime; }
    public void setLessonsAllTime(int lessonsAllTime) { this.lessonsAllTime = lessonsAllTime; }

    public int getLessonsSinceConversion() { return lessonsSinceConversion; }
    public void setLessonsSinceConversion(int lessonsSinceConversion) { 
        this.lessonsSinceConversion = lessonsSinceConversion; 
    }

    public int getPostOnboardLessonCount() { return postOnboardLessonCount; }
    public void setPostOnboardLessonCount(int postOnboardLessonCount) { 
        this.postOnboardLessonCount = postOnboardLessonCount; 
    }

    public Integer getCachedBuxBalanceQuarters() { return cachedBuxBalanceQuarters; }
    public void setCachedBuxBalanceQuarters(Integer cachedBuxBalanceQuarters) { 
        this.cachedBuxBalanceQuarters = cachedBuxBalanceQuarters; 
    }

    public Integer getCachedLegacyBalance() { return cachedLegacyBalance; }
    public void setCachedLegacyBalance(Integer cachedLegacyBalance) { 
        this.cachedLegacyBalance = cachedLegacyBalance; 
    }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }

    public String getLockReason() { return lockReason; }
    public void setLockReason(String lockReason) { this.lockReason = lockReason; }

    public boolean isPostLegacyAlternator() { return postLegacyAlternator; }
    public void setPostLegacyAlternator(boolean postLegacyAlternator) { 
        this.postLegacyAlternator = postLegacyAlternator; 
    }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}


