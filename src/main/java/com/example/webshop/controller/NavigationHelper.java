package com.example.webshop.controller;

import com.example.webshop.dto.CategoryDto;
import com.example.webshop.security.ShopUserDetails;
import com.example.webshop.service.CartService;
import com.example.webshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides data for the shared navigation bar (categories, cart item count).
 * Used directly from the layout fragment as ${@navigation...}, so it is available
 * on every page, including error pages.
 */
@Component("navigation")
@RequiredArgsConstructor
public class NavigationHelper {

    private final CategoryService categoryService;
    private final CartService cartService;

    public List<CategoryDto> activeCategories() {
        return categoryService.findActive();
    }

    public long cartItemCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ShopUserDetails user) {
            return cartService.countItems(user.getId());
        }
        return 0;
    }
}
