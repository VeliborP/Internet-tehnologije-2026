package com.example.webshop.config;

import com.example.webshop.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/** Small helper for resolving localized texts from messages.properties in Java code. */
@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    public String get(BusinessException ex) {
        return get(ex.getMessageKey(), ex.getArgs());
    }
}
