package com.example.NinjaBux.dto;

import jakarta.validation.constraints.NotNull;

public class PurchaseRequest {
    @NotNull(message = "Ninja ID is required")
    private Long ninjaId;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    public PurchaseRequest() {}

    public PurchaseRequest(Long ninjaId, Long itemId) {
        this.ninjaId = ninjaId;
        this.itemId = itemId;
    }

    public Long getNinjaId() { return ninjaId; }
    public void setNinjaId(Long ninjaId) { this.ninjaId = ninjaId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}
