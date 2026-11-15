package com.example.webshop.controller;

import com.example.webshop.TestDataFactory;
import com.example.webshop.dto.OrderDto;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.repository.OrderRepository;
import com.example.webshop.repository.ProductRepository;
import com.example.webshop.repository.UserRepository;
import com.example.webshop.security.ShopUserDetails;
import com.example.webshop.service.CartService;
import com.example.webshop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Controller tests for the catalog, cart, checkout, orders and registration. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShopFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestDataFactory data;

    private User customer;
    private User otherCustomer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = data.createUser("flow-customer", Role.CUSTOMER);
        otherCustomer = data.createUser("flow-other", Role.CUSTOMER);
        Category category = data.createCategory("FLOW");
        product = data.createProduct("FLOW-001", category, "1500.00", 3);
    }

    @Test
    void catalogShowsProductAndSerbianInterface() throws Exception {
        mockMvc.perform(get("/products").param("q", "flow-001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Product FLOW-001")))
                .andExpect(content().string(containsString("1.500,00 RSD")))
                .andExpect(content().string(containsString("Prijavite se za kupovinu")));
    }

    @Test
    void productDetailIsNotFoundForInactiveProduct() throws Exception {
        mockMvc.perform(get("/products/" + product.getId())).andExpect(status().isOk());

        product.setActive(false);
        productRepository.saveAndFlush(product);

        mockMvc.perform(get("/products/" + product.getId())).andExpect(status().isNotFound());
    }

    @Test
    void customerCanAddUpdateAndRemoveCartItem() throws Exception {
        ShopUserDetails principal = new ShopUserDetails(customer);

        mockMvc.perform(post("/cart/add").param("productId", product.getId().toString()).param("quantity", "2")
                        .with(user(principal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));
        assertThat(cartService.countItems(customer.getId())).isEqualTo(2);

        mockMvc.perform(get("/cart").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Product FLOW-001")))
                .andExpect(content().string(containsString("3.000,00 RSD")));

        Long itemId = cartService.getCart(customer.getId()).getItems().get(0).getId();
        mockMvc.perform(post("/cart/items/" + itemId + "/update").param("quantity", "3")
                        .with(user(principal)).with(csrf()))
                .andExpect(redirectedUrl("/cart"));
        assertThat(cartService.countItems(customer.getId())).isEqualTo(3);

        mockMvc.perform(post("/cart/items/" + itemId + "/remove").with(user(principal)).with(csrf()))
                .andExpect(redirectedUrl("/cart"));
        assertThat(cartService.countItems(customer.getId())).isZero();
    }

    @Test
    void addingMoreThanStockShowsErrorAndKeepsCartUnchanged() throws Exception {
        mockMvc.perform(post("/cart/add").param("productId", product.getId().toString()).param("quantity", "4")
                        .with(user(new ShopUserDetails(customer))).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        assertThat(cartService.countItems(customer.getId())).isZero();
    }

    @Test
    void userCannotModifyAnotherUsersCartItem() throws Exception {
        cartService.addProduct(customer.getId(), product.getId(), 1);
        Long itemId = cartService.getCart(customer.getId()).getItems().get(0).getId();

        mockMvc.perform(post("/cart/items/" + itemId + "/remove")
                        .with(user(new ShopUserDetails(otherCustomer))).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(cartService.countItems(customer.getId())).isEqualTo(1);
    }

    @Test
    void checkoutCreatesOrderAndShowsConfirmation() throws Exception {
        ShopUserDetails principal = new ShopUserDetails(customer);
        cartService.addProduct(customer.getId(), product.getId(), 2);

        mockMvc.perform(get("/checkout").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("order/checkout"));

        // A price sent by the client is ignored: the order is always priced from the database.
        mockMvc.perform(post("/checkout/confirm").param("unitPrice", "1.00").param("totalAmount", "1.00")
                        .with(user(principal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*/confirmation"));

        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(orderRepository.findAll().get(0).getTotalAmount()).isEqualByComparingTo("3000.00");
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(1);
        assertThat(cartService.countItems(customer.getId())).isZero();
    }

    @Test
    void checkoutWithEmptyCartRedirectsBackToCart() throws Exception {
        mockMvc.perform(get("/checkout").with(user(new ShopUserDetails(customer))))
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void userCannotViewAnotherUsersOrder() throws Exception {
        cartService.addProduct(customer.getId(), product.getId(), 1);
        OrderDto order = orderService.placeOrder(customer.getId());

        mockMvc.perform(get("/orders/" + order.getId()).with(user(new ShopUserDetails(customer))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(order.getOrderNumber())));

        mockMvc.perform(get("/orders/" + order.getId()).with(user(new ShopUserDetails(otherCustomer))))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/orders/" + order.getId() + "/confirmation").with(user(new ShopUserDetails(otherCustomer))))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/admin/orders/" + order.getId()).with(user(new ShopUserDetails(otherCustomer))))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationIgnoresSubmittedRoleAndCreatesCustomer() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("firstName", "Mila").param("lastName", "Milić")
                        .param("username", "mila").param("password", "lozinka1").param("confirmPassword", "lozinka1")
                        .param("role", "ADMIN"))
                .andExpect(redirectedUrl("/login"));

        assertThat(userRepository.findByUsername("mila").orElseThrow().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void registrationRejectsDuplicateUsernameAndInvalidData() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("firstName", "Mila").param("lastName", "Milić")
                        .param("username", "flow-customer").param("password", "lozinka1")
                        .param("confirmPassword", "lozinka1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("firstName", "").param("lastName", "Milić")
                        .param("username", "mila2").param("password", "123").param("confirmPassword", "456"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registrationForm", "firstName", "password"))
                .andExpect(content().string(containsString("Ovo polje je obavezno.")));
    }
}
