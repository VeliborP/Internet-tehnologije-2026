package com.example.webshop.controller;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.CheckoutReview;
import com.example.webshop.dto.OrderDto;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.security.ShopUserDetails;
import com.example.webshop.service.CartService;
import com.example.webshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final Messages messages;

    /** Order review: availability and current prices are checked again before the page is shown. */
    @GetMapping
    public String review(@AuthenticationPrincipal ShopUserDetails principal, Model model,
                         RedirectAttributes redirectAttributes) {
        CheckoutReview review = cartService.refreshForCheckout(principal.getId());
        if (review.cart().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    review.notices().isEmpty()
                            ? messages.get("checkout.error.empty")
                            : String.join(" ", review.notices()));
            return "redirect:/cart";
        }
        model.addAttribute("cart", review.cart());
        model.addAttribute("notices", review.notices());
        return "order/checkout";
    }

    @PostMapping("/confirm")
    public String confirm(@AuthenticationPrincipal ShopUserDetails principal,
                          RedirectAttributes redirectAttributes) {
        try {
            OrderDto order = orderService.placeOrder(principal.getId());
            return "redirect:/orders/" + order.getId() + "/confirmation";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get(ex));
            return "redirect:/checkout";
        }
    }
}
