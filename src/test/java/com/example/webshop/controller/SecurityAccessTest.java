package com.example.webshop.controller;

import com.example.webshop.TestDataFactory;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.repository.UserRepository;
import com.example.webshop.security.ShopUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Authentication and authorization rules. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestDataFactory data;

    private User admin;
    private User customer;

    @BeforeEach
    void setUp() {
        admin = data.createUser("sec-admin", Role.ADMIN);
        customer = data.createUser("sec-customer", Role.CUSTOMER);
    }

    @Test
    void publicPagesAreAvailableToAnonymousVisitors() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
        mockMvc.perform(get("/products")).andExpect(status().isOk()).andExpect(view().name("products"));
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void anonymousVisitorIsRedirectedToLoginForProtectedPages() throws Exception {
        for (String url : new String[]{"/cart", "/checkout", "/orders", "/profile", "/admin", "/admin/products"}) {
            mockMvc.perform(get(url))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    void customerCannotAccessAdministration() throws Exception {
        for (String url : new String[]{"/admin", "/admin/products", "/admin/categories", "/admin/users", "/admin/orders"}) {
            mockMvc.perform(get(url).with(user(new ShopUserDetails(customer))))
                    .andExpect(status().isForbidden());
        }
        mockMvc.perform(post("/admin/users/" + admin.getId() + "/toggle")
                        .with(user(new ShopUserDetails(customer))).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdministration() throws Exception {
        for (String url : new String[]{"/admin", "/admin/products", "/admin/categories", "/admin/users", "/admin/orders"}) {
            mockMvc.perform(get(url).with(user(new ShopUserDetails(admin))))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void customerCanAccessOwnPages() throws Exception {
        for (String url : new String[]{"/cart", "/orders", "/profile", "/profile/edit"}) {
            mockMvc.perform(get(url).with(user(new ShopUserDetails(customer))))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/cart/add").param("productId", "1").with(user(new ShopUserDetails(customer))))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginSucceedsWithValidCredentials() throws Exception {
        mockMvc.perform(formLogin("/login").user("sec-customer").password(TestDataFactory.PASSWORD))
                .andExpect(authenticated().withRoles("CUSTOMER"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(formLogin("/login").user("sec-customer").password("wrong-password"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void disabledUserCannotLogIn() throws Exception {
        customer.setEnabled(false);
        userRepository.saveAndFlush(customer);

        mockMvc.perform(formLogin("/login").user("sec-customer").password(TestDataFactory.PASSWORD))
                .andExpect(unauthenticated());
    }
}
