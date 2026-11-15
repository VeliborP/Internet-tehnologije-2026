package com.example.webshop.service;

import com.example.webshop.dto.OrderDto;
import com.example.webshop.entity.Cart;
import com.example.webshop.entity.CartItem;
import com.example.webshop.entity.Order;
import com.example.webshop.entity.OrderItem;
import com.example.webshop.entity.Product;
import com.example.webshop.enums.OrderStatus;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.InsufficientStockException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.mapper.OrderMapper;
import com.example.webshop.repository.CartItemRepository;
import com.example.webshop.repository.CartRepository;
import com.example.webshop.repository.OrderRepository;
import com.example.webshop.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private static final String ORDER_NUMBER_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter ORDER_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates an order from the user's cart. Everything happens in ONE database transaction:
     * order + order items are created, stock is decreased and the cart is emptied.
     * Any exception rolls back all changes.
     * <p>
     * Concurrency: product rows are locked first (SELECT ... FOR UPDATE), so two simultaneous
     * checkouts cannot both take the last pieces from stock. Prices and stock are read again
     * from the database - values coming from the browser are never trusted.
     */
    @Transactional
    public OrderDto placeOrder(Long userId) {
        List<Long> productIds = cartItemRepository.findProductIdsByUserId(userId);
        if (productIds.isEmpty()) {
            throw new BusinessException("checkout.error.empty");
        }
        // 1. Lock the products before anything else is loaded, so we work with fresh data.
        Map<Long, Product> lockedProducts = productRepository.findAllByIdInForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 2. Load the cart (its items now reference the locked product instances).
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new BusinessException("checkout.error.empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("checkout.error.empty");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = lockedProducts.get(item.getProduct().getId());
            if (product == null) {
                // The cart was modified in another request while checkout was in progress.
                throw new BusinessException("checkout.error.changed");
            }
            if (!product.isActive() || !product.getCategory().isActive()) {
                throw new BusinessException("checkout.error.unavailable", product.getName());
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName(), product.getStockQuantity());
            }
            if (item.getUnitPrice().compareTo(product.getPrice()) != 0) {
                // The customer has not seen the new price yet - send them back to the review page.
                throw new BusinessException("checkout.error.price.changed", product.getName());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductCode(product.getCode());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(lineTotal);
            order.addItem(orderItem);

            // 3. Decrease stock (never below zero - guarded by the check above and a DB constraint).
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            total = total.add(lineTotal);
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // 4. Empty the cart.
        cart.getItems().clear();
        cart.touch();

        return orderMapper.toDetailDto(saved);
    }

    // ----- Customer -----

    public Page<OrderDto> findForUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toSummaryDto);
    }

    /** Returns the order only if it belongs to the given user; otherwise it is reported as not found. */
    public OrderDto getForUser(Long orderId, Long userId) {
        return orderRepository.findDetailByIdAndUserId(orderId, userId)
                .map(orderMapper::toDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("order.error.notfound"));
    }

    // ----- Administration -----

    public Page<OrderDto> search(String orderNumber, String username, Long userId, OrderStatus status,
                                 Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(orderNumber)) {
                predicates.add(cb.like(cb.lower(root.get("orderNumber")),
                        "%" + orderNumber.trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(cb.lower(root.get("user").get("username")),
                        "%" + username.trim().toLowerCase() + "%"));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return orderRepository.findAll(spec, pageable).map(orderMapper::toSummaryDto);
    }

    public OrderDto getById(Long orderId) {
        return orderRepository.findDetailById(orderId)
                .map(orderMapper::toDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("order.error.notfound"));
    }

    public List<OrderDto> findRecent() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(orderMapper::toSummaryDto)
                .toList();
    }

    public long count() {
        return orderRepository.count();
    }

    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /** Sum of all orders that are not cancelled. */
    public BigDecimal totalRevenue() {
        return orderRepository.sumTotalAmountExcludingStatus(OrderStatus.CANCELLED);
    }

    /**
     * Changes the order status. COMPLETED and CANCELLED are final.
     * Cancelling an order returns the ordered quantities to stock.
     */
    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order.error.notfound"));
        if (order.getStatus() == newStatus) {
            return;
        }
        if (order.getStatus().isFinal()) {
            throw new BusinessException("order.error.status.final");
        }
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(newStatus);
    }

    private void restoreStock(Order order) {
        List<Long> productIds = order.getItems().stream()
                .map(OrderItem::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getId)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return;
        }
        Map<Long, Product> lockedProducts = productRepository.findAllByIdInForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }
            Product product = lockedProducts.get(item.getProduct().getId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(ORDER_NUMBER_DATE);
        String orderNumber;
        do {
            StringBuilder suffix = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                suffix.append(ORDER_NUMBER_ALPHABET.charAt(random.nextInt(ORDER_NUMBER_ALPHABET.length())));
            }
            orderNumber = "ORD-" + datePart + "-" + suffix;
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }
}
