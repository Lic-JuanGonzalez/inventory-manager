package com.inventory.management.service;

import com.inventory.management.domain.entity.Product;
import com.inventory.management.domain.entity.ProductCategory;
import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.ProductCategoryRepository;
import com.inventory.management.domain.repository.ProductRepository;
import com.inventory.management.dto.request.CreateProductRequest;
import com.inventory.management.dto.response.PageResponse;
import com.inventory.management.dto.response.ProductResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(String search, Long categoryId, Boolean active, Pageable pageable) {
        return PageResponse.of(
                productRepository.findAllFiltered(search == null ? "" : search, categoryId, active, pageable).map(this::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest req, User currentUser) {
        if (productRepository.existsBySku(req.sku())) {
            throw new BusinessException("SKU ya registrado: " + req.sku(), HttpStatus.CONFLICT);
        }
        ProductCategory category = resolveCategory(req.categoryId());

        Product product = Product.builder()
                .sku(req.sku().toUpperCase())
                .name(req.name())
                .description(req.description())
                .category(category)
                .unitOfMeasure(req.unitOfMeasure())
                .referencePrice(req.referencePrice())
                .active(true)
                .build();
        product = productRepository.save(product);
        auditService.logAsync(currentUser, "CREATE_PRODUCT", "PRODUCT",
                String.valueOf(product.getId()), null, toResponse(product), null);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, CreateProductRequest req, User currentUser) {
        Product product = getOrThrow(id);
        ProductResponse before = toResponse(product);

        if (productRepository.existsBySkuAndIdNot(req.sku(), id)) {
            throw new BusinessException("SKU ya registrado: " + req.sku(), HttpStatus.CONFLICT);
        }
        ProductCategory category = resolveCategory(req.categoryId());

        product.setSku(req.sku().toUpperCase());
        product.setName(req.name());
        product.setDescription(req.description());
        product.setCategory(category);
        product.setUnitOfMeasure(req.unitOfMeasure());
        product.setReferencePrice(req.referencePrice());
        product = productRepository.save(product);

        auditService.logAsync(currentUser, "UPDATE_PRODUCT", "PRODUCT",
                String.valueOf(id), before, toResponse(product), null);
        return toResponse(product);
    }

    @Transactional
    public void toggleActive(Long id, User currentUser) {
        Product product = getOrThrow(id);
        product.setActive(!product.getActive());
        productRepository.save(product);
        auditService.logAsync(currentUser,
                product.getActive() ? "ACTIVATE_PRODUCT" : "DEACTIVATE_PRODUCT",
                "PRODUCT", String.valueOf(id), null, null, null);
    }

    public Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private ProductCategory resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", categoryId));
    }

    public ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getUnitOfMeasure(), p.getReferencePrice(), p.getActive(), p.getCreatedAt()
        );
    }
}
