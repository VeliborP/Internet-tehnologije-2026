package com.example.webshop.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CartDto {

    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int totalQuantity;

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public static CartDto empty() {
        return new CartDto();
    }
}
