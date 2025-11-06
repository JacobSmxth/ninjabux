package com.example.NinjaBux.exception;

public class ShopItemUnavailableException extends RuntimeException {
    public ShopItemUnavailableException(String itemName) {
        super("Item is currently unavailable: " + itemName);
    }

    public ShopItemUnavailableException(String message, boolean custom) {
        super(message);
    }
}
