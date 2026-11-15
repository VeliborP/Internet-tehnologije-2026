package com.example.webshop.dto;

import com.example.webshop.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderDto {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private Long userId;
    private String username;
    private String customerName;
    private List<OrderItemDto> items = new ArrayList<>();
}
