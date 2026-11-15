package com.example.webshop.controller;

import com.example.webshop.service.CategoryService;
import com.example.webshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private static final int PAGE_SIZE = 12;

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/products")
    public String list(@RequestParam(name = "categoryId", required = false) Long categoryId,
                       @RequestParam(name = "q", defaultValue = "") String q,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("name"));
        model.addAttribute("page", productService.searchActive(categoryId, q, pageable));
        model.addAttribute("categories", categoryService.findActive());
        model.addAttribute("q", q.trim());
        model.addAttribute("categoryId", categoryId);
        if (categoryId != null) {
            model.addAttribute("selectedCategory", categoryService.getActive(categoryId));
        }
        return "products";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("product", productService.getActive(id));
        return "product-detail";
    }
}
