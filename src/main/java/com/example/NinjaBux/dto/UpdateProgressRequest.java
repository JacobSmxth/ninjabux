package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.enums.BeltType;

public class UpdateProgressRequest {
    private BeltType beltType;
    private int level;
    private int lesson;

    public UpdateProgressRequest() {}

    public UpdateProgressRequest(BeltType beltType, int level, int lesson) {
        this.beltType = beltType;
        this.level = level;
        this.lesson = lesson;
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
}
