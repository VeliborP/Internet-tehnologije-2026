package com.example.webshop.service;

import com.example.webshop.config.Messages;
import com.example.webshop.dto.CartDto;
import com.example.webshop.dto.CheckoutReview;
import com.example.webshop.entity.Cart;
import com.example.webshop.entity.CartItem;
import com.example.webshop.entity.Product;
import com.example.webshop.exception.BusinessException;
import com.example.webshop.exception.InsufficientStockException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.mapper.CartMapper;
import com.example.webshop.repository.CartItemRepository;
import com.example.webshop.repository.CartRepository;
import com.example.webshop.repository.ProductRepository;
import com.example.webshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Shopping cart business logic. Every method takes the id of the authenticated user,
 * so a user can never read or modify somebody else's cart.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final Messages messages;

    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .map(cartMapper::toDto)
                .orElseGet(CartDto::empty);
    }

    /** Total quantity of all products in the user's cart (shown in the navigation bar). */
    @Transactional(readOnly = true)
    public long countItems(Long userId) {
        return cartItemRepository.sumQuantityByUserId(userId);
    }

    /**
     * Adds a product to the cart. If the product is already in the cart its quantity is increased.
     * The price is always read from the database, never from the client.
     */
    public void addProduct(Long userId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("cart.error.quantity.min");
        }
        Product product = productRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product.error.notfound"));
        assertPurchasable(product);

        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        long newQuantity = (long) quantity + (item == null ? 0 : item.getQuantity());
        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity());
        }
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            cart.getItems().add(item);
        }
        item.setQuantity((int) newQuantity);
        item.setUnitPrice(product.getPrice());
        // The cart is a managed entity: the new item is persisted on flush (cascade = ALL).
        cart.touch();
    }

    public void updateQuantity(Long userId, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("cart.error.quantity.min");
        }
        CartItem item = getOwnedItem(userId, itemId);
        Product product = item.getProduct();
        assertPurchasable(product);
        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity());
        }
        item.setQuantity(quantity);
        item.getCart().touch();
    }

    public void removeItem(Long userId, Long itemId) {
        CartItem item = getOwnedItem(userId, itemId);
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cart.touch();
    }

    /**
     * Re-validates the cart against the current database state before checkout:
     * unavailable products are removed, quantities are reduced to the available stock
     * and prices are refreshed. Returns the refreshed cart and notices about the changes.
     */
    public CheckoutReview refreshForCheckout(Long userId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId).orElse(null);
        if (cart == null) {
            return new CheckoutReview(CartDto.empty(), List.of());
        }
        List<String> notices = new ArrayList<>();
        boolean changed = false;
        for (Iterator<CartItem> iterator = cart.getItems().iterator(); iterator.hasNext(); ) {
            CartItem item = iterator.next();
            Product product = item.getProduct();
            if (!product.isActive() || !product.getCategory().isActive() || product.getStockQuantity() <= 0) {
                iterator.remove();
                notices.add(messages.get("checkout.notice.removed", product.getName()));
                changed = true;
                continue;
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                item.setQuantity(product.getStockQuantity());
                notices.add(messages.get("checkout.notice.quantity", product.getName(), product.getStockQuantity()));
                changed = true;
            }
            if (item.getUnitPrice().compareTo(product.getPrice()) != 0) {
                item.setUnitPrice(product.getPrice());
                notices.add(messages.get("checkout.notice.price", product.getName()));
                changed = true;
            }
        }
        if (changed) {
            cart.touch();
        }
        return new CheckoutReview(cartMapper.toDto(cart), notices);
    }

    private void assertPurchasable(Product product) {
        if (!product.isActive() || !product.getCategory().isActive()) {
            throw new BusinessException("cart.error.product.inactive", product.getName());
        }
    }

    private CartItem getOwnedItem(Long userId, Long itemId) {
        return cartItemRepository.findOwnedItem(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("cart.error.item.notfound"));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findWithItemsByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userRepository.getReferenceById(userId));
            return cartRepository.save(cart);
        });
    }
}
