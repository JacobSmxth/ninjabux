package com.example.NinjaBux.dto;

import com.example.NinjaBux.domain.LedgerTxn;
import com.example.NinjaBux.domain.enums.LedgerSourceType;
import com.example.NinjaBux.domain.enums.LedgerTxnType;

import java.time.LocalDateTime;

public class LedgerTxnResponse {
    private Long id;
    private Long ninjaId;
    private String ninjaFirstName;
    private String ninjaLastName;
    private int amount;
    private LedgerTxnType type;
    private LedgerSourceType sourceType;
    private Long sourceId;
    private String note;
    private LocalDateTime createdAt;

    public LedgerTxnResponse() {}

    public LedgerTxnResponse(LedgerTxn txn) {
        this.id = txn.getId();
        if (txn.getNinja() != null) {
            this.ninjaId = txn.getNinja().getId();
            this.ninjaFirstName = txn.getNinja().getFirstName();
            this.ninjaLastName = txn.getNinja().getLastName();
        }
        this.amount = txn.getAmount();
        this.type = txn.getType();
        this.sourceType = txn.getSourceType();
        this.sourceId = txn.getSourceId();
        this.note = txn.getNote();
        this.createdAt = txn.getCreatedAt();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNinjaId() { return ninjaId; }
    public void setNinjaId(Long ninjaId) { this.ninjaId = ninjaId; }

    public String getNinjaFirstName() { return ninjaFirstName; }
    public void setNinjaFirstName(String ninjaFirstName) { this.ninjaFirstName = ninjaFirstName; }

    public String getNinjaLastName() { return ninjaLastName; }
    public void setNinjaLastName(String ninjaLastName) { this.ninjaLastName = ninjaLastName; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public LedgerTxnType getType() { return type; }
    public void setType(LedgerTxnType type) { this.type = type; }

    public LedgerSourceType getSourceType() { return sourceType; }
    public void setSourceType(LedgerSourceType sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

