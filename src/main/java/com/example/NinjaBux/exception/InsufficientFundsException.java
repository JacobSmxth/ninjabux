package com.example.NinjaBux.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(int balance, int required) {
        super(String.format("Insufficient funds. Balance: %d Bux, Required: %d Bux", balance, required));
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}
