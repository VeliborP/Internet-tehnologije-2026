package com.example.webshop.repository;

import com.example.webshop.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    long countByCategoryId(Long categoryId);

    /** Public catalog: only active products in active categories. */
    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where p.active = true and p.category.active = true
              and (:categoryId is null or p.category.id = :categoryId)
              and (lower(p.name) like lower(concat('%', :q, '%'))
                   or lower(p.code) like lower(concat('%', :q, '%')))
            """)
    Page<Product> searchActive(@Param("categoryId") Long categoryId, @Param("q") String q, Pageable pageable);

    /** Admin list: all products regardless of status. */
    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where (:categoryId is null or p.category.id = :categoryId)
              and (lower(p.name) like lower(concat('%', :q, '%'))
                   or lower(p.code) like lower(concat('%', :q, '%')))
            """)
    Page<Product> searchAll(@Param("categoryId") Long categoryId, @Param("q") String q, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where p.active = true and p.featured = true and p.category.active = true
            order by p.id desc
            """)
    List<Product> findFeatured(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findWithCategoryById(@Param("id") Long id);

    /**
     * Locks the given product rows (SELECT ... FOR UPDATE) so stock cannot be changed
     * concurrently while an order is being created. Rows are locked in id order to avoid deadlocks.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop5ByStockQuantityLessThanEqualOrderByStockQuantityAsc(int threshold);
}
