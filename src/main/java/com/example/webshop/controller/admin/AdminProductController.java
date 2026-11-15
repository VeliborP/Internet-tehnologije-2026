package com.example.webshop.controller.admin;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.CategoryDto;
import com.example.webshop.dto.ProductForm;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.FieldBusinessException;
import com.example.webshop.service.CategoryService;
import com.example.webshop.service.ProductService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private static final int PAGE_SIZE = 10;
    private static final String FORM_VIEW = "admin/products/form";
    private static final String REDIRECT_LIST = "redirect:/admin/products";

    private final ProductService productService;
    private final CategoryService categoryService;
    private final Messages messages;

    @ModelAttribute("categories")
    public List<CategoryDto> categories() {
        return categoryService.findAll();
    }

    @GetMapping
    public String list(@RequestParam(name = "q", defaultValue = "") String q,
                       @RequestParam(name = "categoryId", required = false) Long categoryId,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("name"));
        model.addAttribute("page", productService.searchAll(categoryId, q, pageable));
        model.addAttribute("q", q.trim());
        model.addAttribute("categoryId", categoryId);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult result,
                         @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            productService.create(form, imageFile);
        } catch (BusinessException ex) {
            applyError(result, ex);
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.product.created"));
        return REDIRECT_LIST;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("productForm", productService.getForm(id));
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult result,
                         @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                         RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            productService.update(id, form, imageFile);
        } catch (BusinessException ex) {
            applyError(result, ex);
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.product.updated"));
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean active = productService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage",
                messages.get(active ? "admin.product.activated" : "admin.product.deactivated"));
        return REDIRECT_LIST;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.product.deleted"));
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
