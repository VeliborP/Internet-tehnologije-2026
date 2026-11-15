package com.example.webshop.mapper;

final class MapperUtils {

    static final String PLACEHOLDER_IMAGE = "/images/placeholder.svg";

    private MapperUtils() {
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String imageOrPlaceholder(String image) {
        return trimToNull(image) == null ? PLACEHOLDER_IMAGE : image;
    }

    static String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1).trim() + "…";
    }
}
