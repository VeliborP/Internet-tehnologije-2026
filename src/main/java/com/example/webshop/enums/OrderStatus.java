package com.example.webshop.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED;

    /** Final statuses cannot be changed anymore. */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
