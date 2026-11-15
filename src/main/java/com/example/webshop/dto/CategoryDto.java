package com.example.webshop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private long productCount;
}
