package com.example.webshop.dto;

import com.example.webshop.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Admin form for creating and editing users. */
@Getter
@Setter
public class UserForm {

    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size.max}")
    private String firstName;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size.max}")
    private String lastName;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "{validation.username.pattern}")
    private String username;

    /** Required when creating a user; leave empty on edit to keep the current password. */
    @Pattern(regexp = "^$|^.{6,72}$", message = "{validation.password.size}")
    private String password;

    @NotNull(message = "{validation.required}")
    private Role role = Role.CUSTOMER;

    private boolean enabled = true;
}
