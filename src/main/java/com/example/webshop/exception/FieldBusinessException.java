package com.example.webshop.exception;

import lombok.Getter;

/** Business rule violation that is related to a single form field. */
@Getter
public class FieldBusinessException extends BusinessException {

    /** Name of the form field the error belongs to. */
    private final String field;

    public FieldBusinessException(String field, String messageKey, Object... args) {
        super(messageKey, args);
        this.field = field;
    }
}
