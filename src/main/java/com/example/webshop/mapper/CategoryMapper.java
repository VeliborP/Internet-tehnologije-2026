package com.example.webshop.mapper;

import com.example.webshop.dto.CategoryDto;
import com.example.webshop.dto.CategoryForm;
import com.example.webshop.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setCode(category.getCode());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.isActive());
        return dto;
    }

    public CategoryForm toForm(Category category) {
        CategoryForm form = new CategoryForm();
        form.setId(category.getId());
        form.setCode(category.getCode());
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        form.setActive(category.isActive());
        return form;
    }

    public void applyForm(CategoryForm form, Category category) {
        category.setCode(form.getCode().trim());
        category.setName(form.getName().trim());
        category.setDescription(MapperUtils.trimToNull(form.getDescription()));
        category.setActive(form.isActive());
    }
}
