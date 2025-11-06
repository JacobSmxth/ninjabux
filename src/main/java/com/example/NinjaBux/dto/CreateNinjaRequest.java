package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.enums.BeltType;

public class CreateNinjaRequest {
    private String firstName;
    private String lastName;
    private String username;
    private BeltType beltType;
    private Integer level;
    private Integer lesson;

    public CreateNinjaRequest() {}

    public CreateNinjaRequest(String firstName, String lastName, String username, BeltType beltType, Integer level, Integer lesson) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.beltType = beltType;
        this.level = level;
        this.lesson = lesson;
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
    public BeltType getBeltType() {
        return beltType;
    }
    public void setBeltType(BeltType beltType) {
        this.beltType = beltType;
    }

    public Integer getLevel() {
        return level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public Integer getLesson() {
        return lesson;
    }
    public void setLesson(Integer lesson) {
        this.lesson = lesson;
    }
}
