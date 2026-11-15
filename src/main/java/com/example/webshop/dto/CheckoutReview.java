package com.example.webshop.dto;

import java.util.List;

/**
 * Result of re-validating the cart before checkout: the refreshed cart and
 * localized notices describing what changed (price, quantity, removed products).
 */
public record CheckoutReview(CartDto cart, List<String> notices) {
}
