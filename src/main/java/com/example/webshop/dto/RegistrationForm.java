package com.example.webshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Public registration form. It deliberately has no role field: new accounts are always CUSTOMER. */
@Getter
@Setter
public class RegistrationForm {

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size.max}")
    private String firstName;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size.max}")
    private String lastName;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "{validation.username.pattern}")
    private String username;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 72, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.required}")
    private String confirmPassword;
}
