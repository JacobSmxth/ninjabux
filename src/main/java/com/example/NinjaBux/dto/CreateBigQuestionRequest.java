package com.example.NinjaBux.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateBigQuestionRequest {
    private LocalDate questionDate;
    private String questionText;

    // I removed short answers, so this means nothing but im scared to removed
    private String questionType;
    private String correctAnswer;

    private Integer correctChoiceIndex;
    private List<String> choices;
    private Long suggestionId;

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
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getCorrectChoiceIndex() {
        return correctChoiceIndex;
    }
    public void setCorrectChoiceIndex(Integer correctChoiceIndex) {
        this.correctChoiceIndex = correctChoiceIndex;
    }
    public List<String> getChoices() {
        return choices;
    }
    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public Long getSuggestionId() {
        return suggestionId;
    }
    public void setSuggestionId(Long suggestionId) {
        this.suggestionId = suggestionId;
    }
}
