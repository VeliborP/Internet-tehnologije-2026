package com.example.webshop.exception;

import lombok.Getter;

/** Thrown when a requested resource does not exist or is not accessible to the current user. */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String messageKey;

    public ResourceNotFoundException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }
}
