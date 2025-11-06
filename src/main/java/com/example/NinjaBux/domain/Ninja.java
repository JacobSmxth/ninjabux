package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.BeltType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

// WHY: remove deprecated balance fields and ledger cache fields to simplify domain model
// WHAT: removed totalBuxEarned/totalBuxSpent, cached balances, lesson counters, lock metadata, adminNote
@Entity
@Table(name = "ninjas",
indexes = {
        @Index(name = "idx_ninjas_username", columnList = "username", unique = true)
})
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

    private int currentLevel;
    private int currentLesson;

    @Enumerated(EnumType.STRING)
    private BeltType currentBeltType;

    private LocalDateTime createdAt;
    private LocalDateTime lastProgressUpdate;

    private int totalQuestionsAnswered = 0;
    private int totalQuestionsCorrect = 0;

    private boolean suggestionsBanned = false;

    private int legacyLessons = 0;

    private int totalLessons = 0;

    private boolean isLocked = false;



    public Ninja() {}

    public Ninja(String firstName, String lastName, String username, Integer currentLesson, Integer currentLevel, BeltType currentBeltType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.currentLesson = currentLesson;
        this.currentLevel = currentLevel;
        this.currentBeltType = currentBeltType;
        this.totalQuestionsAnswered = 0;
        this.totalQuestionsCorrect = 0;
        this.isLocked = false;
        setTimestamps();
    }


    // WHY: centralize timestamp initialization to avoid duplication
    // WHAT: moved timestamp logic from constructor to @PrePersist hook
    @PrePersist
    public void setTimestamps() {
        this.createdAt = LocalDateTime.now();
        this.lastProgressUpdate = LocalDateTime.now();
    }

    // WHY: encapsulate quiz answer tracking logic in domain model
    // WHAT: added recordAnswer method to update counters atomically
    public void recordAnswer(boolean correct) {
        this.totalQuestionsAnswered++;
        if (correct) {
            this.totalQuestionsCorrect++;
        }
    }

    // WHY: provide domain methods for account state transitions
    // WHAT: added lock/unlock methods to encapsulate state changes
    public void lock() {
        this.isLocked = true;
    }

    public void unlock() {
        this.isLocked = false;
    }

    public Long getId() {return id;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public String getUsername() {return username;}
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

    public int getLegacyLessons() { return legacyLessons; }
    public void setLegacyLessons(int legacyLessons) {this.legacyLessons = legacyLessons;}

    public int getTotalLessons() {return totalLessons;}
    public void setTotalLessons(int totalLessons) {this.totalLessons = totalLessons;}


    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }

}


