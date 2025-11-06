package com.example.NinjaBux.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_answers")
public class QuestionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private BigQuestion question;

    @ManyToOne
    @JoinColumn(name = "ninja_id", nullable = false)
    private Ninja ninja;

    @Column(nullable = false, length = 1000)
    private String answer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime answeredAt;

    public QuestionAnswer() {}

    public QuestionAnswer(BigQuestion question, Ninja ninja, String answer, boolean correct) {
        this.question = question;
        this.ninja = ninja;
        this.answer = answer;
        this.correct = correct;
        this.answeredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigQuestion getQuestion() {
        return question;
    }

    public void setQuestion(BigQuestion question) {
        this.question = question;
    }

    public Ninja getNinja() {
        return ninja;
    }

    public void setNinja(Ninja ninja) {
        this.ninja = ninja;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
