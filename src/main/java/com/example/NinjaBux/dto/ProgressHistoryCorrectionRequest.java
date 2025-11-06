package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.enums.BeltType;

public class ProgressHistoryCorrectionRequest {
    private Long originalEntryId;
    private BeltType beltType;
    private int level;
    private int lesson;
    private int buxDelta;
    private Integer legacyDelta;
    private String notes;
    private String adminUsername;

    public ProgressHistoryCorrectionRequest() {}

    public Long getOriginalEntryId() {
        return originalEntryId;
    }

    public void setOriginalEntryId(Long originalEntryId) {
        this.originalEntryId = originalEntryId;
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

    public int getBuxDelta() {
        return buxDelta;
    }

    public void setBuxDelta(int buxDelta) {
        this.buxDelta = buxDelta;
    }

    public Integer getLegacyDelta() {
        return legacyDelta;
    }

    public void setLegacyDelta(Integer legacyDelta) {
        this.legacyDelta = legacyDelta;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
}
