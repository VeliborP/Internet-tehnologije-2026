package com.example.webshop.controller.admin;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.CategoryForm;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.FieldBusinessException;
import com.example.webshop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private static final String FORM_VIEW = "admin/categories/form";
    private static final String REDIRECT_LIST = "redirect:/admin/categories";

    private final CategoryService categoryService;
    private final Messages messages;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            categoryService.create(form);
        } catch (FieldBusinessException ex) {
            result.rejectValue(ex.getField(), ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.category.created"));
        return REDIRECT_LIST;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("categoryForm", categoryService.getForm(id));
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            categoryService.update(id, form);
        } catch (FieldBusinessException ex) {
            result.rejectValue(ex.getField(), ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.category.updated"));
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean active = categoryService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage",
                messages.get(active ? "admin.category.activated" : "admin.category.deactivated"));
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.category.deleted"));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get(ex));
        }
        return REDIRECT_LIST;
    }
}
