package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.domain.enums.BeltType;

import java.time.LocalDateTime;

public class ProgressHistoryResponse {
    private Long id;
    private BeltType beltType;
    private int level;
    private int lesson;
    private int buxEarned;
    private String earningType;
    private LocalDateTime timestamp;
    private String notes;
    private Integer legacyDelta;
    private Long correctionToId;
    private String adminUsername;
    private boolean isCorrection;

    public ProgressHistoryResponse() {}

    public ProgressHistoryResponse(ProgressHistory history) {
        this.id = history.getId();
        this.beltType = history.getBeltType();
        this.level = history.getLevel();
        this.lesson = history.getLesson();
        this.buxEarned = history.getBuxEarned();
        this.earningType = history.getEarningType() != null ? history.getEarningType().toString() : "LEVEL_UP";
        this.timestamp = history.getTimestamp();
        this.notes = history.getNotes();
        this.legacyDelta = history.getLegacyDelta();
        this.correctionToId = history.getCorrectionToId();
        this.adminUsername = history.getAdminUsername();
        this.isCorrection = history.isCorrection();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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

    public String getEarningType() {
        return earningType;
    }
    public void setEarningType(String earningType) {
        this.earningType = earningType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
