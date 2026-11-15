package com.example.webshop.controller.admin;

import com.example.webshop.enums.OrderStatus;
import com.example.webshop.service.CategoryService;
import com.example.webshop.service.OrderService;
import com.example.webshop.service.ProductService;
import com.example.webshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("categoryCount", categoryService.count());
        model.addAttribute("userCount", userService.count());
        model.addAttribute("orderCount", orderService.count());
        model.addAttribute("pendingOrderCount", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("revenue", orderService.totalRevenue());
        model.addAttribute("recentOrders", orderService.findRecent());
        model.addAttribute("lowStockProducts", productService.findLowStock());
        return "admin/dashboard";
    }
}
