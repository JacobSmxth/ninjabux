package com.example.NinjaBux.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "big_questions")
public class BigQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate questionDate;

    @Column(nullable = false, length = 500)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Column(length = 1000)
    private String correctAnswer;

    @Column
    private Integer correctChoiceIndex;

    @ElementCollection
    @CollectionTable(name = "question_choices", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "choice")
    private List<String> choices = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private LocalDate weekStartDate;
    
    @Column
    private LocalDate weekEndDate;

    @Column
    private Long suggestedByNinjaId;
    
    @Enumerated(EnumType.STRING)
    @Column
    private QuestionStatus status = QuestionStatus.APPROVED;
    
    @Column
    private String approvedByAdmin;
    
    @Column
    private String rejectionReason;

    public enum QuestionType {
        MULTIPLE_CHOICE,
        SHORT_ANSWER
    }

    public enum QuestionStatus {
        PENDING,
        APPROVED,
        REJECTED,
    }

    public BigQuestion() {}

    public BigQuestion(LocalDate questionDate, String questionText, QuestionType questionType, String correctAnswer) {
        this.questionDate = questionDate;
        this.questionText = questionText;
        this.questionType = questionType;
        this.correctAnswer = correctAnswer;
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
    public QuestionType getQuestionType() {
        return questionType;
    }
    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    public List<String> getChoices() {
        return choices;
    }
    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
    public Integer getCorrectChoiceIndex() {
        return correctChoiceIndex;
    }
    public void setCorrectChoiceIndex(Integer correctChoiceIndex) {
        this.correctChoiceIndex = correctChoiceIndex;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
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
    public Long getSuggestedByNinjaId() {
        return suggestedByNinjaId;
    }
    public void setSuggestedByNinjaId(Long suggestedByNinjaId) {
        this.suggestedByNinjaId = suggestedByNinjaId;
    }
    public QuestionStatus getStatus() {
        return status;
    }
    public void setStatus(QuestionStatus status) {
        this.status = status;
    }
    public String getApprovedByAdmin() {
        return approvedByAdmin;
    }
    public void setApprovedByAdmin(String approvedByAdmin) {
        this.approvedByAdmin = approvedByAdmin;
    }
    public String getRejectionReason() {
        return rejectionReason;
    }
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
