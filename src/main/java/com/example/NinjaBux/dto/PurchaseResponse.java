package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.Purchase;
import com.example.NinjaBux.domain.enums.PurchaseStatus;
import java.time.LocalDateTime;

public class PurchaseResponse {
    private Long id;
    private Long ninjaId;
    private String ninjaName;
    private Long itemId;
    private String itemName;
    private String itemDescription;
    private int pricePaid;
    private LocalDateTime purchaseDate;
    private PurchaseStatus status;
    private LocalDateTime redeemedDate;

    public PurchaseResponse(Purchase purchase) {
        this.id = purchase.getId();
        this.ninjaId = purchase.getNinja().getId();
        this.ninjaName = purchase.getNinja().getFirstName() + " " + purchase.getNinja().getLastName();
        this.itemId = purchase.getShopItem().getId();
        this.itemName = purchase.getShopItem().getName();
        this.itemDescription = purchase.getShopItem().getDescription();
        this.pricePaid = purchase.getPricePaid();
        this.purchaseDate = purchase.getPurchaseDate();
        this.status = purchase.getStatus();
        this.redeemedDate = purchase.getRedeemedDate();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNinjaId() { return ninjaId; }
    public void setNinjaId(Long ninjaId) { this.ninjaId = ninjaId; }

    public String getNinjaName() { return ninjaName; }
    public void setNinjaName(String ninjaName) { this.ninjaName = ninjaName; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public int getPricePaid() { return pricePaid; }
    public void setPricePaid(int pricePaid) { this.pricePaid = pricePaid; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public PurchaseStatus getStatus() { return status; }
    public void setStatus(PurchaseStatus status) { this.status = status; }

    public LocalDateTime getRedeemedDate() { return redeemedDate; }
    public void setRedeemedDate(LocalDateTime redeemedDate) { this.redeemedDate = redeemedDate; }
}
