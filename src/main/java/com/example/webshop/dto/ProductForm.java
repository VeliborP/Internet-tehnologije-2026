package com.example.webshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductForm {

    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size.max}")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "{validation.code.pattern}")
    private String code;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size.max}")
    private String name;

    @Size(max = 2000, message = "{validation.size.max}")
    private String description;

    /** Existing image path or external URL; an uploaded file takes precedence. */
    @Size(max = 500, message = "{validation.size.max}")
    @Pattern(regexp = "^$|^(https?://|/images/|/uploads/)[^\\s\"'<>]*$", message = "{validation.image.url}")
    private String image;

    @NotNull(message = "{validation.required}")
    @DecimalMin(value = "0.00", message = "{validation.price.min}")
    @Digits(integer = 10, fraction = 2, message = "{validation.price.digits}")
    private BigDecimal price;

    @NotNull(message = "{validation.required}")
    @Min(value = 0, message = "{validation.stock.min}")
    @Max(value = 1000000, message = "{validation.stock.max}")
    private Integer stockQuantity;

    @NotNull(message = "{validation.required}")
    private Long categoryId;

    private boolean active = true;

    private boolean featured;
}
