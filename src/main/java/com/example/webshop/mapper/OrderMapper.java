package com.example.webshop.mapper;

import com.example.webshop.dto.OrderDto;
import com.example.webshop.dto.OrderItemDto;
import com.example.webshop.entity.Order;
import com.example.webshop.entity.OrderItem;
import com.example.webshop.entity.User;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    /** Maps order header data only (used in lists). */
    public OrderDto toSummaryDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.getTotalAmount());
        User user = order.getUser();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setCustomerName(user.getFullName());
        return dto;
    }

    /** Maps the order together with its items (used on detail pages). */
    public OrderDto toDetailDto(Order order) {
        OrderDto dto = toSummaryDto(order);
        for (OrderItem item : order.getItems()) {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setProductCode(item.getProductCode());
            itemDto.setProductName(item.getProductName());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            dto.getItems().add(itemDto);
        }
        return dto;
    }
}
