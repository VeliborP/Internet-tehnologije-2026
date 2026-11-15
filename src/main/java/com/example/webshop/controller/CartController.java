package com.example.webshop.controller;

import com.example.webshop.config.Messages;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.security.ShopUserDetails;
import com.example.webshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Shopping cart. All operations work on the cart of the authenticated user only;
 * the client never sends prices or a cart id.
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final Messages messages;

    @GetMapping
    public String view(@AuthenticationPrincipal ShopUserDetails principal, Model model) {
        model.addAttribute("cart", cartService.getCart(principal.getId()));
        return "cart/view";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal ShopUserDetails principal,
                      @RequestParam("productId") Long productId,
                      @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                      RedirectAttributes redirectAttributes) {
        try {
            cartService.addProduct(principal.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", messages.get("cart.added"));
            redirectAttributes.addFlashAttribute("showCartLink", true);
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get(ex));
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/items/{itemId}/update")
    public String update(@AuthenticationPrincipal ShopUserDetails principal,
                         @PathVariable("itemId") Long itemId,
                         @RequestParam("quantity") int quantity,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(principal.getId(), itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", messages.get("cart.updated"));
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messages.get(ex));
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{itemId}/remove")
    public String remove(@AuthenticationPrincipal ShopUserDetails principal,
                         @PathVariable("itemId") Long itemId,
                         RedirectAttributes redirectAttributes) {
        cartService.removeItem(principal.getId(), itemId);
        redirectAttributes.addFlashAttribute("successMessage", messages.get("cart.removed"));
        return "redirect:/cart";
    }
}
