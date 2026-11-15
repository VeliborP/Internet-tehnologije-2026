package com.example.webshop.controller.admin;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.UserForm;
import com.example.webshop.enums.Role;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.FieldBusinessException;
import com.example.webshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private static final int PAGE_SIZE = 10;
    private static final String FORM_VIEW = "admin/users/form";
    private static final String REDIRECT_LIST = "redirect:/admin/users";

    private final UserService userService;
    private final Messages messages;

    @ModelAttribute("roles")
    public Role[] roles() {
        return Role.values();
    }

    @GetMapping
    public String list(@RequestParam(name = "q", defaultValue = "") String q,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("username"));
        model.addAttribute("page", userService.search(q, pageable));
        model.addAttribute("q", q.trim());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            userService.create(form);
        } catch (BusinessException ex) {
            applyError(result, ex);
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.user.created"));
        return REDIRECT_LIST;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("userForm", userService.getForm(id));
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            userService.update(id, form);
        } catch (BusinessException ex) {
            applyError(result, ex);
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.user.updated"));
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean enabled = userService.toggleEnabled(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    messages.get(enabled ? "admin.user.activated" : "admin.user.deactivated"));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get(ex));
        }
        return REDIRECT_LIST;
    }

    private void applyError(BindingResult result, BusinessException ex) {
        if (ex instanceof FieldBusinessException fieldException) {
            result.rejectValue(fieldException.getField(), ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
        } else {
            result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
        }
    }
}
