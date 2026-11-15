package com.example.webshop.service;

import com.example.webshop.dto.ProductDto;
import com.example.webshop.dto.ProductForm;
import com.example.webshop.entity.Category;
import com.example.webshop.entity.Product;
import com.example.webshop.exception.DuplicateResourceException;
import com.example.webshop.exception.ResourceNotFoundException;
import com.example.webshop.mapper.ProductMapper;
import com.example.webshop.repository.CartItemRepository;
import com.example.webshop.repository.CategoryRepository;
import com.example.webshop.repository.OrderItemRepository;
import com.example.webshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final FileStorageService fileStorageService;
    private final ProductMapper productMapper;

    // ----- Public catalog -----

    public Page<ProductDto> searchActive(Long categoryId, String query, Pageable pageable) {
        return productRepository.searchActive(categoryId, normalize(query), pageable).map(productMapper::toDto);
    }

    public List<ProductDto> findFeatured(int limit) {
        return productRepository.findFeatured(PageRequest.of(0, limit)).stream()
                .map(productMapper::toDto)
                .toList();
    }

    public List<ProductDto> findLatest(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return productRepository.searchActive(null, "", pageable).getContent().stream()
                .map(productMapper::toDto)
                .toList();
    }

    /** Returns a product for the public detail page; inactive products are treated as not found. */
    public ProductDto getActive(Long id) {
        Product product = getEntity(id);
        if (!product.isActive() || !product.getCategory().isActive()) {
            throw new ResourceNotFoundException("product.error.notfound");
        }
        return productMapper.toDto(product);
    }

    // ----- Administration -----

    public Page<ProductDto> searchAll(Long categoryId, String query, Pageable pageable) {
        return productRepository.searchAll(categoryId, normalize(query), pageable).map(productMapper::toDto);
    }

    public List<ProductDto> findLowStock() {
        return productRepository.findTop5ByStockQuantityLessThanEqualOrderByStockQuantityAsc(LOW_STOCK_THRESHOLD)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    public long count() {
        return productRepository.count();
    }

    public ProductForm getForm(Long id) {
        return productMapper.toForm(getEntity(id));
    }

    @Transactional
    public void create(ProductForm form, MultipartFile imageFile) {
        String code = form.getCode().trim();
        if (productRepository.existsByCode(code)) {
            throw new DuplicateResourceException("code", "product.error.code.duplicate", code);
        }
        Product product = new Product();
        productMapper.applyForm(form, product, getCategory(form.getCategoryId()));
        product.setImage(resolveImage(form, imageFile));
        productRepository.save(product);
    }

    @Transactional
    public void update(Long id, ProductForm form, MultipartFile imageFile) {
        Product product = getEntity(id);
        String code = form.getCode().trim();
        if (productRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("code", "product.error.code.duplicate", code);
        }
        String previousImage = product.getImage();
        productMapper.applyForm(form, product, getCategory(form.getCategoryId()));
        product.setImage(resolveImage(form, imageFile));
        if (previousImage != null && !previousImage.equals(product.getImage())) {
            fileStorageService.deleteIfStored(previousImage);
        }
    }

    @Transactional
    public boolean toggleActive(Long id) {
        Product product = getEntity(id);
        product.setActive(!product.isActive());
        return product.isActive();
    }

    /**
     * Permanently deletes a product. Cart items with this product are removed, while order items
     * keep their historical data (code, name, price) and only lose the product reference.
     */
    @Transactional
    public void delete(Long id) {
        String image = getEntity(id).getImage();
        cartItemRepository.deleteByProductId(id);
        orderItemRepository.detachProduct(id);
        productRepository.deleteById(id);
        fileStorageService.deleteIfStored(image);
    }

    private String resolveImage(ProductForm form, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            return fileStorageService.store(imageFile);
        }
        String image = form.getImage() == null ? "" : form.getImage().trim();
        return image.isEmpty() ? null : image;
    }

    private Product getEntity(Long id) {
        return productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product.error.notfound"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category.error.notfound"));
    }

    private String normalize(String query) {
        return query == null ? "" : query.trim();
    }
}
