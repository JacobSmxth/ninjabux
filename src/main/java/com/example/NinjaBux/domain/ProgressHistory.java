package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.BeltType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ProgressHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ninja_id", nullable = false)
    private Ninja ninja;

    @Enumerated(EnumType.STRING)
    private BeltType beltType;

    private int level;
    private int lesson;
    private int buxEarned;

    @Enumerated(EnumType.STRING)
    private EarningType earningType;

    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String notes;

    private Integer legacyDelta;

    @Column(name = "correction_to_id")
    private Long correctionToId;

    private String adminUsername;

    @Column(nullable = false)
    private boolean isCorrection = false;

    public enum EarningType {
        LEVEL_UP,
        ADMIN_AWARD,
        ADMIN_CORRECTION
    }

    public ProgressHistory() {}

    public ProgressHistory(Ninja ninja, BeltType beltType, int level, int lesson, int buxEarned) {
        this.ninja = ninja;
        this.beltType = beltType;
        this.level = level;
        this.lesson = lesson;
        this.buxEarned = buxEarned;
        this.earningType = EarningType.LEVEL_UP;
        this.timestamp = LocalDateTime.now();
    }

    public ProgressHistory(Ninja ninja, BeltType beltType, int level, int lesson, int buxEarned, EarningType earningType) {
        this.ninja = ninja;
        this.beltType = beltType;
        this.level = level;
        this.lesson = lesson;
        this.buxEarned = buxEarned;
        this.earningType = earningType;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Ninja getNinja() {
        return ninja;
    }
    public void setNinja(Ninja ninja) {
        this.ninja = ninja;
    }
    public BeltType getBeltType() {
        return beltType;
    }
    public void setBeltType(BeltType beltType) {
        this.beltType = beltType;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int getLesson() {
        return lesson;
    }
    public void setLesson(int lesson) {
        this.lesson = lesson;
    }
    public int getBuxEarned() {
        return buxEarned;
    }
    public void setBuxEarned(int buxEarned) {
        this.buxEarned = buxEarned;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public EarningType getEarningType() {
        return earningType;
    }
    public void setEarningType(EarningType earningType) {
        this.earningType = earningType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getLegacyDelta() {
        return legacyDelta;
    }

    public void setLegacyDelta(Integer legacyDelta) {
        this.legacyDelta = legacyDelta;
    }

    public Long getCorrectionToId() {
        return correctionToId;
    }

    public void setCorrectionToId(Long correctionToId) {
        this.correctionToId = correctionToId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public boolean isCorrection() {
        return isCorrection;
    }

    public void setCorrection(boolean isCorrection) {
        this.isCorrection = isCorrection;
    }
}
