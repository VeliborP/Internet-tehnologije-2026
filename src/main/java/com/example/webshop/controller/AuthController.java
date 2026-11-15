package com.example.webshop.controller;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.RegistrationForm;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.security.ShopUserDetails;
import com.example.webshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final Messages messages;

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal ShopUserDetails principal) {
        return principal != null ? "redirect:/" : "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
        if (principal != null) {
            return "redirect:/";
        }
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())
                && !result.hasFieldErrors("confirmPassword")) {
            result.rejectValue("confirmPassword", "validation.password.mismatch");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form);
        } catch (BusinessException ex) {
            FormErrors.apply(result, ex);
            return "auth/register";
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("register.success"));
        return "redirect:/login";
    }
}
