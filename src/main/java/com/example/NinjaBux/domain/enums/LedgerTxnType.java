package com.example.NinjaBux.domain.enums;

public enum LedgerTxnType {
    EARN,      // Earned through lessons, achievements, etc.
    SPEND,     // Spent on purchases
    REFUND,    // Refunded from purchases
    ADJUST,    // Admin adjustments
    CONVERT,   // Legacy to Bux conversion
    GRANT      // Initial grant (for Legacy imports)
}

