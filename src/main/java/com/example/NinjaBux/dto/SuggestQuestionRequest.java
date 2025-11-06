package com.example.NinjaBux.dto;

import java.util.List;

public class SuggestQuestionRequest {
    private Long ninjaId;
    private String questionText;
    private String questionType; // MULTIPLE_CHOICE or SHORT_ANSWER
    private String correctAnswer; // For short answer
    private Integer correctChoiceIndex; // For multiple choice (0-based index)
    private List<String> choices;

    public Long getNinjaId() {
        return ninjaId;
    }
    public void setNinjaId(Long ninjaId) {
        this.ninjaId = ninjaId;
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
}

