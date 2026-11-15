package com.example.webshop.service;

import com.example.webshop.TestDataFactory;
import com.example.webshop.dto.CartDto;
import com.example.webshop.dto.CheckoutReview;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.InsufficientStockException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TestDataFactory data;

    private User customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = data.createUser("cart-customer", Role.CUSTOMER);
        Category category = data.createCategory("CART");
        product = data.createProduct("CART-001", category, "250.00", 5);
    }

    @Test
    void addProductCreatesCartItemWithCurrentPrice() {
        cartService.addProduct(customer.getId(), product.getId(), 2);

        CartDto cart = cartService.getCart(customer.getId());
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualByComparingTo("250.00");
        assertThat(cart.getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(cartService.countItems(customer.getId())).isEqualTo(2);
    }

    @Test
    void addingSameProductAgainIncreasesQuantityInsteadOfCreatingNewItem() {
        cartService.addProduct(customer.getId(), product.getId(), 1);
        cartService.addProduct(customer.getId(), product.getId(), 3);

        CartDto cart = cartService.getCart(customer.getId());
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(4);
    }

    @Test
    void cannotAddMoreThanAvailableStock() {
        cartService.addProduct(customer.getId(), product.getId(), 4);

        assertThatThrownBy(() -> cartService.addProduct(customer.getId(), product.getId(), 2))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(cartService.countItems(customer.getId())).isEqualTo(4);
    }

    @Test
    void quantityMustBeAtLeastOne() {
        assertThatThrownBy(() -> cartService.addProduct(customer.getId(), product.getId(), 0))
                .isInstanceOf(BusinessException.class);

        cartService.addProduct(customer.getId(), product.getId(), 1);
        Long itemId = cartService.getCart(customer.getId()).getItems().get(0).getId();
        assertThatThrownBy(() -> cartService.updateQuantity(customer.getId(), itemId, 0))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void inactiveProductCannotBeAdded() {
        product.setActive(false);
        productRepository.saveAndFlush(product);

        assertThatThrownBy(() -> cartService.addProduct(customer.getId(), product.getId(), 1))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateQuantityChecksStock() {
        cartService.addProduct(customer.getId(), product.getId(), 1);
        Long itemId = cartService.getCart(customer.getId()).getItems().get(0).getId();

        cartService.updateQuantity(customer.getId(), itemId, 5);
        assertThat(cartService.countItems(customer.getId())).isEqualTo(5);

        assertThatThrownBy(() -> cartService.updateQuantity(customer.getId(), itemId, 6))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void removeItemEmptiesCart() {
        cartService.addProduct(customer.getId(), product.getId(), 1);
        Long itemId = cartService.getCart(customer.getId()).getItems().get(0).getId();

        cartService.removeItem(customer.getId(), itemId);

        assertThat(cartService.getCart(customer.getId()).isEmpty()).isTrue();
        assertThat(cartService.countItems(customer.getId())).isZero();
    }

    @Test
    void userCannotModifyAnotherUsersCart() {
        User other = data.createUser("cart-other", Role.CUSTOMER);
        cartService.addProduct(customer.getId(), product.getId(), 1);
        Long itemId = cartService.getCart(customer.getId()).getItems().get(0).getId();

        assertThatThrownBy(() -> cartService.updateQuantity(other.getId(), itemId, 2))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> cartService.removeItem(other.getId(), itemId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(cartService.countItems(customer.getId())).isEqualTo(1);
    }

    @Test
    void checkoutReviewRefreshesPriceAndQuantity() {
        cartService.addProduct(customer.getId(), product.getId(), 4);
        product.setPrice(new BigDecimal("300.00"));
        product.setStockQuantity(2);
        productRepository.saveAndFlush(product);

        CheckoutReview review = cartService.refreshForCheckout(customer.getId());

        assertThat(review.notices()).hasSize(2);
        assertThat(review.cart().getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(review.cart().getItems().get(0).getUnitPrice()).isEqualByComparingTo("300.00");
        assertThat(review.cart().getTotalAmount()).isEqualByComparingTo("600.00");
    }
}
