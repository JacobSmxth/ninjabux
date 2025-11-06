package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.BigQuestion;

import java.time.LocalDate;
import java.util.List;

public class BigQuestionResponse {
    private Long id;
    private LocalDate questionDate;
    private String questionText;
    private String questionType;
    private List<String> choices;
    private boolean hasAnswered;
    private boolean wasCorrect;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    public BigQuestionResponse(BigQuestion question, boolean hasAnswered, boolean wasCorrect) {
        this.id = question.getId();
        this.questionDate = question.getQuestionDate();
        this.questionText = question.getQuestionText();
        this.questionType = question.getQuestionType().toString();
        this.choices = question.getChoices();
        this.hasAnswered = hasAnswered;
        this.wasCorrect = wasCorrect;
        this.weekStartDate = question.getWeekStartDate();
        this.weekEndDate = question.getWeekEndDate();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDate getQuestionDate() {
        return questionDate;
    }
    public void setQuestionDate(LocalDate questionDate) {
        this.questionDate = questionDate;
    }

    public String getQuestionText() {
        return questionText;
    }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    public String getQuestionType() {
        return questionType;
    }
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public List<String> getChoices() {
        return choices;
    }
    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
    public boolean isHasAnswered() {
        return hasAnswered;
    }
    public void setHasAnswered(boolean hasAnswered) {
        this.hasAnswered = hasAnswered;
    }

    public boolean isWasCorrect() {
        return wasCorrect;
    }
    public void setWasCorrect(boolean wasCorrect) {
        this.wasCorrect = wasCorrect;
    }
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }
    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }
    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }
}
