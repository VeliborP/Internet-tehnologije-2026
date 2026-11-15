package com.example.webshop;

import com.example.webshop.entity.Cart;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.repository.CartRepository;
import com.example.webshop.repository.CategoryRepository;
import com.example.webshop.repository.ProductRepository;
import com.example.webshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Creates test data. Tests are transactional, so everything is rolled back after each test. */
@Component
public class TestDataFactory {

    public static final String PASSWORD = "secret123";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    public User createUser(String username, Role role) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername(username);
        user.setPassword(PASSWORD);
        user.setRole(role);
        user.setEnabled(true);
        userRepository.saveAndFlush(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.saveAndFlush(cart);
        return user;
    }

    public Category createCategory(String code) {
        Category category = new Category();
        category.setCode(code);
        category.setName("Category " + code);
        category.setActive(true);
        return categoryRepository.saveAndFlush(category);
    }

    public Product createProduct(String code, Category category, String price, int stock) {
        Product product = new Product();
        product.setCode(code);
        product.setName("Product " + code);
        product.setDescription("Description of " + code);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setActive(true);
        return productRepository.saveAndFlush(product);
    }
}
