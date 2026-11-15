package com.example.webshop.service;

import com.example.webshop.TestDataFactory;
import com.example.webshop.dto.RegistrationForm;
import com.example.webshop.dto.UserDto;
import com.example.webshop.dto.UserForm;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.DuplicateResourceException;
import com.example.webshop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestDataFactory data;

    @Test
    void registrationAlwaysCreatesEnabledCustomerWithCart() {
        UserDto user = userService.register(registration("new-customer"));

        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(cartService.getCart(user.getId()).isEmpty()).isTrue();
    }

    @Test
    void usernameMustBeUnique() {
        userService.register(registration("taken"));

        assertThatThrownBy(() -> userService.register(registration("taken")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void lastActiveAdminCannotBeDeactivated() {
        User admin = data.createUser("only-admin", Role.ADMIN);

        assertThatThrownBy(() -> userService.toggleEnabled(admin.getId()))
                .isInstanceOf(BusinessException.class);
        assertThat(userRepository.findById(admin.getId()).orElseThrow().isEnabled()).isTrue();
    }

    @Test
    void lastActiveAdminCannotBeDemoted() {
        User admin = data.createUser("only-admin", Role.ADMIN);
        UserForm form = userService.getForm(admin.getId());
        form.setRole(Role.CUSTOMER);

        assertThatThrownBy(() -> userService.update(admin.getId(), form))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void adminCanBeDeactivatedWhenAnotherActiveAdminExists() {
        User firstAdmin = data.createUser("admin-one", Role.ADMIN);
        data.createUser("admin-two", Role.ADMIN);

        boolean enabled = userService.toggleEnabled(firstAdmin.getId());

        assertThat(enabled).isFalse();
    }

    private RegistrationForm registration(String username) {
        RegistrationForm form = new RegistrationForm();
        form.setFirstName("Ana");
        form.setLastName("Anić");
        form.setUsername(username);
        form.setPassword("secret123");
        form.setConfirmPassword("secret123");
        return form;
    }
}
