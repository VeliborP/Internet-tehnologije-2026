package com.example.webshop.mapper;

import com.example.webshop.dto.ProductDto;
import com.example.webshop.dto.ProductForm;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private static final int SHORT_DESCRIPTION_LENGTH = 90;

    public ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setCode(product.getCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setShortDescription(MapperUtils.abbreviate(product.getDescription(), SHORT_DESCRIPTION_LENGTH));
        dto.setImageUrl(MapperUtils.imageOrPlaceholder(product.getImage()));
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());
        Category category = product.getCategory();
        dto.setCategoryId(category.getId());
        dto.setCategoryName(category.getName());
        dto.setCategoryActive(category.isActive());
        return dto;
    }

    public ProductForm toForm(Product product) {
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setCode(product.getCode());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setImage(product.getImage());
        form.setPrice(product.getPrice());
        form.setStockQuantity(product.getStockQuantity());
        form.setCategoryId(product.getCategory().getId());
        form.setActive(product.isActive());
        form.setFeatured(product.isFeatured());
        return form;
    }

    /** Copies form values to the entity. The image is handled separately by the service. */
    public void applyForm(ProductForm form, Product product, Category category) {
        product.setCode(form.getCode().trim());
        product.setName(form.getName().trim());
        product.setDescription(MapperUtils.trimToNull(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setStockQuantity(form.getStockQuantity());
        product.setCategory(category);
        product.setActive(form.isActive());
        product.setFeatured(form.isFeatured());
    }
}
