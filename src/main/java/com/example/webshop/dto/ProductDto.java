package com.example.webshop.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDto {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private Long id;
    private String code;
    private String name;
    private String description;
    private String shortDescription;
    /** Image path/URL that is always safe to render (falls back to a placeholder). */
    private String imageUrl;
    private BigDecimal price;
    private int stockQuantity;
    private boolean active;
    private boolean featured;
    private Long categoryId;
    private String categoryName;
    private boolean categoryActive;

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public boolean isLowStock() {
        return stockQuantity > 0 && stockQuantity <= LOW_STOCK_THRESHOLD;
    }
}
