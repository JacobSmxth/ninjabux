package com.example.NinjaBux.exception;

public class ShopItemNotFoundException extends RuntimeException {
    public ShopItemNotFoundException(Long id) {
        super("Shop item not found with id: " + id);
    }

    public ShopItemNotFoundException(String message) {
        super(message);
    }
}
