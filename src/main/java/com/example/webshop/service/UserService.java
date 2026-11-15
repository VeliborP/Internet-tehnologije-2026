package com.example.webshop.service;

import com.example.webshop.dto.PasswordChangeForm;
import com.example.webshop.dto.ProfileForm;
import com.example.webshop.dto.RegistrationForm;
import com.example.webshop.dto.UserDto;
import com.example.webshop.dto.UserForm;
import com.example.webshop.entity.Cart;
import com.example.webshop.entity.User;
import com.example.webshop.enums.Role;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.DuplicateResourceException;
import com.example.webshop.exception.FieldBusinessException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.mapper.UserMapper;
import com.example.webshop.repository.CartRepository;
import com.example.webshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ----- Registration and profile -----

    /** Registers a new customer. The role is always CUSTOMER, regardless of the submitted data. */
    @Transactional
    public UserDto register(RegistrationForm form) {
        return userMapper.toDto(createUser(form.getFirstName(), form.getLastName(), form.getUsername(),
                form.getPassword(), Role.CUSTOMER, true));
    }

    public UserDto getById(Long id) {
        return userMapper.toDto(getEntity(id));
    }

    public ProfileForm getProfileForm(Long id) {
        return userMapper.toProfileForm(getEntity(id));
    }

    @Transactional
    public void updateProfile(Long userId, ProfileForm form) {
        User user = getEntity(userId);
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeForm form) {
        User user = getEntity(userId);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new FieldBusinessException("currentPassword", "profile.error.password.current");
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
    }

    // ----- Administration -----

    public Page<UserDto> search(String query, Pageable pageable) {
        String q = query == null ? "" : query.trim();
        return userRepository.search(q, pageable).map(userMapper::toDto);
    }

    public long count() {
        return userRepository.count();
    }

    public UserForm getForm(Long id) {
        return userMapper.toForm(getEntity(id));
    }

    @Transactional
    public void create(UserForm form) {
        if (!StringUtils.hasText(form.getPassword())) {
            throw new FieldBusinessException("password", "validation.required");
        }
        createUser(form.getFirstName(), form.getLastName(), form.getUsername(), form.getPassword(),
                form.getRole(), form.isEnabled());
    }

    @Transactional
    public void update(Long id, UserForm form) {
        User user = getEntity(id);
        String username = form.getUsername().trim();
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new DuplicateResourceException("username", "user.error.username.duplicate", username);
        }
        assertNotLastActiveAdmin(user, form.getRole(), form.isEnabled());
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setUsername(username);
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());
        if (StringUtils.hasText(form.getPassword())) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
    }

    @Transactional
    public boolean toggleEnabled(Long id) {
        User user = getEntity(id);
        boolean newEnabled = !user.isEnabled();
        assertNotLastActiveAdmin(user, user.getRole(), newEnabled);
        user.setEnabled(newEnabled);
        return newEnabled;
    }

    /**
     * Prevents removing the last active ADMIN account (by deactivation or role change).
     * Active admins are locked for the duration of the transaction, so two concurrent
     * requests cannot remove the last two admins at the same time.
     */
    private void assertNotLastActiveAdmin(User user, Role newRole, boolean newEnabled) {
        boolean isActiveAdmin = user.getRole() == Role.ADMIN && user.isEnabled();
        boolean staysActiveAdmin = newRole == Role.ADMIN && newEnabled;
        if (isActiveAdmin && !staysActiveAdmin
                && userRepository.findEnabledByRoleForUpdate(Role.ADMIN).size() <= 1) {
            throw new BusinessException("user.error.last.admin");
        }
    }

    private User createUser(String firstName, String lastName, String username, String rawPassword,
                            Role role, boolean enabled) {
        String normalizedUsername = username.trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DuplicateResourceException("username", "user.error.username.duplicate", normalizedUsername);
        }
        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);

        // Every user gets a cart right away (User - Cart is one-to-one).
        Cart cart = new Cart();
        cart.setUser(saved);
        cartRepository.save(cart);
        return saved;
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.error.notfound"));
    }
}
