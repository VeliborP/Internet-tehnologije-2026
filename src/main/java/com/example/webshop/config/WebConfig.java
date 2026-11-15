package com.example.webshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.nio.file.Paths;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Default UI language: Serbian, Latin script. */
    public static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("sr-Latn-RS");

    private final String uploadDir;

    public WebConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * The UI is currently available only in Serbian (Latin). To add more languages later,
     * replace this bean with a SessionLocaleResolver + LocaleChangeInterceptor and add
     * messages_xx.properties files.
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(DEFAULT_LOCALE);
    }

    /** Serves uploaded product images from the external upload directory. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
