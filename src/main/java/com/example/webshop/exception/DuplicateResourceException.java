package com.example.webshop.exception;

/** Thrown when a unique value (username, product code, category code) already exists. */
public class DuplicateResourceException extends FieldBusinessException {

    public DuplicateResourceException(String field, String messageKey, Object... args) {
        super(field, messageKey, args);
    }
}
