package com.example.webshop.repository;

import com.example.webshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Removes the product reference from historical order items before a product is deleted. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OrderItem oi set oi.product = null where oi.product.id = :productId")
    void detachProduct(@Param("productId") Long productId);
}
