package com.example.webshop.controller;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.PasswordChangeForm;
import com.example.webshop.dto.ProfileForm;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final Messages messages;

    @GetMapping
    public String profile(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
        model.addAttribute("user", userService.getById(principal.getId()));
        return "user/profile";
    }

    @GetMapping("/edit")
    public String editForm(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
        model.addAttribute("profileForm", userService.getProfileForm(principal.getId()));
        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        return "user/edit";
    }

    @PostMapping("/edit")
    public String update(@AuthenticationPrincipal ShopUserDetails principal,
                         @Valid @ModelAttribute("profileForm") ProfileForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("passwordChangeForm", new PasswordChangeForm());
            return "user/edit";
        }
        userService.updateProfile(principal.getId(), form);
        redirectAttributes.addFlashAttribute("successMessage", messages.get("profile.updated"));
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal ShopUserDetails principal,
                                 @Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (form.getNewPassword() != null && !form.getNewPassword().equals(form.getConfirmPassword())
                && !result.hasFieldErrors("confirmPassword")) {
            result.rejectValue("confirmPassword", "validation.password.mismatch");
        }
        if (!result.hasErrors()) {
            try {
                userService.changePassword(principal.getId(), form);
            } catch (BusinessException ex) {
                FormErrors.apply(result, ex);
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("profileForm", userService.getProfileForm(principal.getId()));
            return "user/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("profile.password.changed"));
        return "redirect:/profile";
    }
}
