package com.example.webshop.controller.admin;

import com.example.webshop.config.Messages;
import com.example.webshop.enums.OrderStatus;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private static final int PAGE_SIZE = 10;

    private final OrderService orderService;
    private final Messages messages;

    @ModelAttribute("statuses")
    public OrderStatus[] statuses() {
        return OrderStatus.values();
    }

    @GetMapping
    public String list(@RequestParam(name = "q", defaultValue = "") String q,
                       @RequestParam(name = "username", defaultValue = "") String username,
                       @RequestParam(name = "userId", required = false) Long userId,
                       @RequestParam(name = "status", required = false) OrderStatus status,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("page", orderService.search(q, username, userId, status, pageable));
        model.addAttribute("q", q.trim());
        model.addAttribute("username", username.trim());
        model.addAttribute("userId", userId);
        model.addAttribute("status", status);
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("order", orderService.getById(id));
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", messages.get("admin.order.status.updated"));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get(ex));
        }
        return "redirect:/admin/orders/" + id;
    }
}
