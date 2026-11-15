package com.example.webshop.controller;

import com.example.webshop.security.ShopUserDetails;
import com.example.webshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Order history of the authenticated user. Orders of other users are reported as not found (404). */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final int PAGE_SIZE = 10;

    private final OrderService orderService;

    @GetMapping
    public String list(@AuthenticationPrincipal ShopUserDetails principal,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("page", orderService.findForUser(principal.getId(), pageable));
        return "order/list";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal ShopUserDetails principal,
                         @PathVariable("id") Long id, Model model) {
        model.addAttribute("order", orderService.getForUser(id, principal.getId()));
        return "order/detail";
    }

    @GetMapping("/{id}/confirmation")
    public String confirmation(@AuthenticationPrincipal ShopUserDetails principal,
                               @PathVariable("id") Long id, Model model) {
        model.addAttribute("order", orderService.getForUser(id, principal.getId()));
        return "order/confirmation";
    }
}
