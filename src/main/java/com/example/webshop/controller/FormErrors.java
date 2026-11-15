package com.example.webshop.controller;

import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.FieldBusinessException;
import org.springframework.validation.BindingResult;

/** Translates business exceptions thrown by services into form (binding) errors. */
final class FormErrors {

    private FormErrors() {
    }

    static void apply(BindingResult result, BusinessException ex) {
        if (ex instanceof FieldBusinessException fieldException) {
            result.rejectValue(fieldException.getField(), ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
        } else {
            result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
        }
    }
}
