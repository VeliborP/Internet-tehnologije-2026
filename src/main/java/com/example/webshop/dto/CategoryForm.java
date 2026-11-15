package com.example.webshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryForm {

    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size.max}")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "{validation.code.pattern}")
    private String code;

    @NotBlank(message = "{validation.required}")
    @Size(max = 150, message = "{validation.size.max}")
    private String name;

    @Size(max = 1000, message = "{validation.size.max}")
    private String description;

    private boolean active = true;
}
