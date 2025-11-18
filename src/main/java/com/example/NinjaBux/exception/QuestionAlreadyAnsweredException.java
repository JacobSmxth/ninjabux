package com.example.NinjaBux.exception;

public class QuestionAlreadyAnsweredException extends RuntimeException {
    public QuestionAlreadyAnsweredException(String message) {
        super(message);
    }
}
