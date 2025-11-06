package com.example.NinjaBux.exception;

public class NinjaNotFoundException extends RuntimeException {
    public NinjaNotFoundException(Long id) {
        super("Ninja not found with id: " + id);
    }

    public NinjaNotFoundException(String message) {
        super(message);
    }
}
