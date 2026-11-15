package com.example.webshop.mapper;

import com.example.webshop.dto.CartDto;
import com.example.webshop.dto.CartItemDto;
import com.example.webshop.entity.Cart;
import com.example.webshop.entity.CartItem;
import com.example.webshop.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartMapper {

    public CartDto toDto(Cart cart) {
        CartDto dto = new CartDto();
        BigDecimal total = BigDecimal.ZERO;
        int quantity = 0;
        for (CartItem item : cart.getItems()) {
            CartItemDto itemDto = toItemDto(item);
            dto.getItems().add(itemDto);
            total = total.add(itemDto.getLineTotal());
            quantity += item.getQuantity();
        }
        dto.setTotalAmount(total);
        dto.setTotalQuantity(quantity);
        return dto;
    }

    private CartItemDto toItemDto(CartItem item) {
        Product product = item.getProduct();
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setProductId(product.getId());
        dto.setProductCode(product.getCode());
        dto.setProductName(product.getName());
        dto.setImageUrl(MapperUtils.imageOrPlaceholder(product.getImage()));
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        dto.setAvailableStock(product.getStockQuantity());
        return dto;
    }
}
