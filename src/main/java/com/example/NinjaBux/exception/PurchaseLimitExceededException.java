package com.example.NinjaBux.exception;


// not quite implemented fully
public class PurchaseLimitExceededException extends RuntimeException {
    private final String limitType;
    private final int current;
    private final int max;

    public PurchaseLimitExceededException(String limitType, int current, int max) {
        super(String.format("Purchase limit exceeded: %s. Current: %d, Maximum: %d", limitType, current, max));
        this.limitType = limitType;
        this.current = current;
        this.max = max;
    }

    public PurchaseLimitExceededException(String message) {
        super(message);
        this.limitType = null;
        this.current = 0;
        this.max = 0;
    }

    public String getLimitType() { return limitType; }
    public int getCurrent() { return current; }
    public int getMax() { return max; }
}

