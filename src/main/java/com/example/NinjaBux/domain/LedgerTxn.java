package com.example.NinjaBux.domain;

import com.example.NinjaBux.domain.enums.LedgerSourceType;
import com.example.NinjaBux.domain.enums.LedgerTxnType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_txn")
public class LedgerTxn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ninja_id", nullable = false)
    private Ninja ninja;

    /**
     * Amount in quarters (1.0 Bux = 4 quarters, 0.5 Bux = 2 quarters, 0.25 Bux = 1 quarter)
     * Positive for EARN/REFUND, negative for SPEND
     */
    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerTxnType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerSourceType sourceType;

    /**
     * ID of the source entity (e.g., Purchase.id, Achievement.id, ProgressHistory.id)
     * Can be null for admin adjustments
     */
    private Long sourceId;

    /**
     * Human-readable note describing the transaction
     */
    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public LedgerTxn() {}

    public LedgerTxn(Ninja ninja, int amount, LedgerTxnType type, LedgerSourceType sourceType, Long sourceId, String note) {
        this.ninja = ninja;
        this.amount = amount;
        this.type = type;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ninja getNinja() { return ninja; }
    public void setNinja(Ninja ninja) { this.ninja = ninja; }

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

    /**
     * Helper method to convert Bux to quarters
     */
    public static int buxToQuarters(double bux) {
        return (int) Math.round(bux * 4);
    }

    /**
     * Helper method to convert quarters to Bux
     */
    public static double quartersToBux(int quarters) {
        return quarters / 4.0;
    }
}

