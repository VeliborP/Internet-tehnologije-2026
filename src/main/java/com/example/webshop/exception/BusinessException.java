package com.example.webshop.exception;

import lombok.Getter;

/**
 * Thrown when a business rule is violated. Carries a message key (messages.properties)
 * and optional arguments, so the user-facing text stays localized.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] args;

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
}
