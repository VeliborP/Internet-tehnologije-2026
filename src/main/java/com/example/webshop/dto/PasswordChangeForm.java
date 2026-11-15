package com.example.webshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeForm {

    @NotBlank(message = "{validation.required}")
    private String currentPassword;

    @NotBlank(message = "{validation.required}")
    @Size(min = 6, max = 72, message = "{validation.password.size}")
    private String newPassword;

    @NotBlank(message = "{validation.required}")
    private String confirmPassword;
}
