package com.example.webshop.controller;

import com.example.webshop.service.CategoryService;
import com.example.webshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final int HOME_PRODUCT_COUNT = 8;

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.findActive());
        model.addAttribute("featuredProducts", productService.findFeatured(HOME_PRODUCT_COUNT));
        model.addAttribute("latestProducts", productService.findLatest(HOME_PRODUCT_COUNT));
        return "index";
    }

    /** Target of Spring Security's access denied handling (HTTP 403). */
    @RequestMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
