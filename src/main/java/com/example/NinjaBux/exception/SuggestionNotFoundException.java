package com.example.NinjaBux.exception;

public class SuggestionNotFoundException extends RuntimeException {
    public SuggestionNotFoundException(String message) {
        super(message);
    }
}
