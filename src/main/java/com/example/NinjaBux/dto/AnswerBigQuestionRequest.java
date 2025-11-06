package com.example.NinjaBux.dto;

public class AnswerBigQuestionRequest {
    private Long ninjaId;
    private String answer;

    public Long getNinjaId() {
        return ninjaId;
    }
    public void setNinjaId(Long ninjaId) {
        this.ninjaId = ninjaId;
    }
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
