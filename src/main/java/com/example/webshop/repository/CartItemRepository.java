package com.example.webshop.repository;

import com.example.webshop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Finds a cart item only if it belongs to the given user (prevents access to other users' carts). */
    @Query("select i from CartItem i join fetch i.cart c join fetch i.product where i.id = :id and c.user.id = :userId")
    Optional<CartItem> findOwnedItem(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select coalesce(sum(i.quantity), 0) from CartItem i where i.cart.user.id = :userId")
    long sumQuantityByUserId(@Param("userId") Long userId);

    @Query("select i.product.id from CartItem i where i.cart.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CartItem i where i.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
