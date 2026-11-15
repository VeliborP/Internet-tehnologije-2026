package com.example.webshop.service;

import com.example.webshop.TestDataFactory;
import com.example.webshop.dto.OrderDto;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import com.example.webshop.entity.User;
import com.example.webshop.enums.OrderStatus;
import com.example.webshop.enums.Role;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.InsufficientStockException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.repository.OrderRepository;
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
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TestDataFactory data;

    private User customer;
    private Product first;
    private Product second;

    @BeforeEach
    void setUp() {
        customer = data.createUser("order-customer", Role.CUSTOMER);
        Category category = data.createCategory("ORD");
        first = data.createProduct("ORD-001", category, "100.00", 10);
        second = data.createProduct("ORD-002", category, "49.50", 3);
    }

    @Test
    void placeOrderCreatesOrderDecreasesStockAndEmptiesCart() {
        cartService.addProduct(customer.getId(), first.getId(), 2);
        cartService.addProduct(customer.getId(), second.getId(), 3);

        OrderDto order = orderService.placeOrder(customer.getId());

        assertThat(order.getOrderNumber()).startsWith("ORD-");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("348.50");
        assertThat(order.getItems().get(0).getProductCode()).isEqualTo("ORD-001");
        assertThat(order.getItems().get(0).getTotalPrice()).isEqualByComparingTo("200.00");

        assertThat(productRepository.findById(first.getId()).orElseThrow().getStockQuantity()).isEqualTo(8);
        assertThat(productRepository.findById(second.getId()).orElseThrow().getStockQuantity()).isZero();
        assertThat(cartService.getCart(customer.getId()).isEmpty()).isTrue();
    }

    @Test
    void placeOrderFailsForEmptyCart() {
        assertThatThrownBy(() -> orderService.placeOrder(customer.getId()))
                .isInstanceOf(BusinessException.class);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void placeOrderFailsWhenStockIsNoLongerSufficient() {
        cartService.addProduct(customer.getId(), first.getId(), 5);
        first.setStockQuantity(4);
        productRepository.saveAndFlush(first);

        assertThatThrownBy(() -> orderService.placeOrder(customer.getId()))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(orderRepository.count()).isZero();
        assertThat(productRepository.findById(first.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);
        assertThat(cartService.countItems(customer.getId())).isEqualTo(5);
    }

    @Test
    void placeOrderFailsWhenPriceChangedAfterReview() {
        cartService.addProduct(customer.getId(), first.getId(), 1);
        first.setPrice(new BigDecimal("120.00"));
        productRepository.saveAndFlush(first);

        assertThatThrownBy(() -> orderService.placeOrder(customer.getId()))
                .isInstanceOf(BusinessException.class);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void orderKeepsHistoricalDataAfterProductIsDeleted() {
        cartService.addProduct(customer.getId(), first.getId(), 1);
        OrderDto order = orderService.placeOrder(customer.getId());

        productService.delete(first.getId());

        OrderDto reloaded = orderService.getForUser(order.getId(), customer.getId());
        assertThat(reloaded.getItems()).hasSize(1);
        assertThat(reloaded.getItems().get(0).getProductCode()).isEqualTo("ORD-001");
        assertThat(reloaded.getItems().get(0).getUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(reloaded.getTotalAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void userCannotSeeAnotherUsersOrder() {
        User other = data.createUser("order-other", Role.CUSTOMER);
        cartService.addProduct(customer.getId(), first.getId(), 1);
        OrderDto order = orderService.placeOrder(customer.getId());

        assertThat(orderService.getForUser(order.getId(), customer.getId()).getId()).isEqualTo(order.getId());
        assertThatThrownBy(() -> orderService.getForUser(order.getId(), other.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancellingOrderRestoresStockAndStatusBecomesFinal() {
        cartService.addProduct(customer.getId(), first.getId(), 4);
        OrderDto order = orderService.placeOrder(customer.getId());
        assertThat(productRepository.findById(first.getId()).orElseThrow().getStockQuantity()).isEqualTo(6);

        orderService.updateStatus(order.getId(), OrderStatus.CANCELLED);

        assertThat(productRepository.findById(first.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
        assertThatThrownBy(() -> orderService.updateStatus(order.getId(), OrderStatus.SHIPPED))
                .isInstanceOf(BusinessException.class);
    }
}
