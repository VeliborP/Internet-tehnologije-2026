package com.example.webshop.repository;

import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Category activeCategory;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        activeCategory = category("ACT", true);
        inactiveCategory = category("INA", false);
        product("LAP-001", "Laptop Pro", activeCategory, true);
        product("MOU-001", "Wireless mouse", activeCategory, true);
        product("OLD-001", "Old laptop", activeCategory, false);
        product("HID-001", "Hidden laptop", inactiveCategory, true);
    }

    @Test
    void searchActiveReturnsOnlyActiveProductsInActiveCategories() {
        Page<Product> page = productRepository.searchActive(null, "", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Product::getCode)
                .containsExactlyInAnyOrder("LAP-001", "MOU-001");
    }

    @Test
    void searchActiveMatchesNameAndCodeIgnoringCase() {
        assertThat(productRepository.searchActive(null, "LAPTOP", PageRequest.of(0, 10)).getContent())
                .extracting(Product::getCode).containsExactly("LAP-001");
        assertThat(productRepository.searchActive(null, "mou-0", PageRequest.of(0, 10)).getContent())
                .extracting(Product::getCode).containsExactly("MOU-001");
    }

    @Test
    void searchActiveFiltersByCategory() {
        assertThat(productRepository.searchActive(activeCategory.getId(), "", PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(2);
        assertThat(productRepository.searchActive(inactiveCategory.getId(), "", PageRequest.of(0, 10))
                .getTotalElements()).isZero();
    }

    @Test
    void searchAllIncludesInactiveProducts() {
        assertThat(productRepository.searchAll(null, "", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(4);
    }

    @Test
    void searchResultsArePaginated() {
        Page<Product> page = productRepository.searchAll(null, "", PageRequest.of(0, 3));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void productCodeMustBeUnique() {
        Product duplicate = new Product();
        duplicate.setCode("LAP-001");
        duplicate.setName("Duplicate");
        duplicate.setPrice(new BigDecimal("10.00"));
        duplicate.setStockQuantity(1);
        duplicate.setCategory(activeCategory);

        assertThatThrownBy(() -> productRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Category category(String code, boolean active) {
        Category category = new Category();
        category.setCode(code);
        category.setName("Category " + code);
        category.setActive(active);
        return categoryRepository.saveAndFlush(category);
    }

    private void product(String code, String name, Category category, boolean active) {
        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(10);
        product.setCategory(category);
        product.setActive(active);
        productRepository.saveAndFlush(product);
    }
}
