package com.example.webshop.exception;

/** Thrown when the requested quantity exceeds the available stock. */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productName, int availableQuantity) {
        super(availableQuantity > 0 ? "cart.error.stock" : "cart.error.out.of.stock", productName, availableQuantity);
    }
}
