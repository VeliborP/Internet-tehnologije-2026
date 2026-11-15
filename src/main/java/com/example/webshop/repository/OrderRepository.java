package com.example.webshop.repository;

import com.example.webshop.entity.Order;
import com.example.webshop.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "user")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("select o from Order o left join fetch o.items join fetch o.user where o.id = :id")
    Optional<Order> findDetailById(@Param("id") Long id);

    /** Loads an order only if it belongs to the given user (prevents access to other users' orders). */
    @Query("select o from Order o left join fetch o.items join fetch o.user where o.id = :id and o.user.id = :userId")
    Optional<Order> findDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<Order> findTop5ByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status <> :excluded")
    BigDecimal sumTotalAmountExcludingStatus(@Param("excluded") OrderStatus excluded);
}
