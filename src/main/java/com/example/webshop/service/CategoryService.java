package com.example.webshop.service;

import com.example.webshop.dto.CategoryDto;
import com.example.webshop.dto.CategoryForm;
import com.example.webshop.entity.Category;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.DuplicateResourceException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.mapper.CategoryMapper;
import com.example.webshop.repository.CategoryRepository;
import com.example.webshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    /** Active categories for the public part of the shop. */
    public List<CategoryDto> findActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    /** All categories with product counts for the admin part. */
    public List<CategoryDto> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> {
                    CategoryDto dto = categoryMapper.toDto(category);
                    dto.setProductCount(productRepository.countByCategoryId(category.getId()));
                    return dto;
                })
                .toList();
    }

    public long count() {
        return categoryRepository.count();
    }

    public CategoryDto getActive(Long id) {
        Category category = getEntity(id);
        if (!category.isActive()) {
            throw new ResourceNotFoundException("category.error.notfound");
        }
        return categoryMapper.toDto(category);
    }

    public CategoryForm getForm(Long id) {
        return categoryMapper.toForm(getEntity(id));
    }

    @Transactional
    public void create(CategoryForm form) {
        String code = form.getCode().trim();
        if (categoryRepository.existsByCode(code)) {
            throw new DuplicateResourceException("code", "category.error.code.duplicate", code);
        }
        Category category = new Category();
        categoryMapper.applyForm(form, category);
        categoryRepository.save(category);
    }

    @Transactional
    public void update(Long id, CategoryForm form) {
        Category category = getEntity(id);
        String code = form.getCode().trim();
        if (categoryRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("code", "category.error.code.duplicate", code);
        }
        categoryMapper.applyForm(form, category);
    }

    @Transactional
    public boolean toggleActive(Long id) {
        Category category = getEntity(id);
        category.setActive(!category.isActive());
        return category.isActive();
    }

    /** A category can be deleted only when it has no products; otherwise it should be deactivated. */
    @Transactional
    public void delete(Long id) {
        Category category = getEntity(id);
        if (productRepository.countByCategoryId(id) > 0) {
            throw new BusinessException("category.error.has.products", category.getName());
        }
        categoryRepository.delete(category);
    }

    private Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category.error.notfound"));
    }
}
