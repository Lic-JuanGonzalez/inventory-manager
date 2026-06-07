package com.inventory.management.service;

import com.inventory.management.domain.entity.Product;
import com.inventory.management.domain.entity.ProductCategory;
import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.ProductCategoryRepository;
import com.inventory.management.domain.repository.ProductRepository;
import com.inventory.management.dto.request.CreateProductRequest;
import com.inventory.management.dto.response.ProductResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductCategoryRepository categoryRepository;
    @Mock AuditService auditService;

    @InjectMocks ProductService productService;

    private User currentUser;
    private Product product;
    private ProductCategory category;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).name("Admin").lastName("Test")
                .email("admin@test.com").build();
        category = ProductCategory.builder().id(1L).name("Electronics").build();
        product = Product.builder().id(1L).sku("TEST-001").name("Test Product")
                .category(category).unitOfMeasure("UNIDAD")
                .referencePrice(new BigDecimal("100.00")).active(true).build();
    }

    @Test
    void create_withValidData_returnsProductResponse() {
        CreateProductRequest req = new CreateProductRequest(
                "NEW-001", "New Product", "desc", 1L, "UNIDAD", new BigDecimal("50.00"));

        when(productRepository.existsBySku("NEW-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(
                Product.builder().id(2L).sku("NEW-001").name("New Product")
                        .category(category).unitOfMeasure("UNIDAD")
                        .referencePrice(new BigDecimal("50.00")).active(true).build());

        ProductResponse response = productService.create(req, currentUser);

        assertThat(response.sku()).isEqualTo("NEW-001");
        assertThat(response.name()).isEqualTo("New Product");
        verify(productRepository).save(any(Product.class));
        verify(auditService).logAsync(any(), eq("CREATE_PRODUCT"), eq("PRODUCT"), any(), any(), any(), any());
    }

    @Test
    void create_withDuplicateSku_throwsBusinessException() {
        CreateProductRequest req = new CreateProductRequest(
                "TEST-001", "Dup", null, null, "UNIDAD", BigDecimal.TEN);
        when(productRepository.existsBySku("TEST-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(req, currentUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU already registered");

        verify(productRepository, never()).save(any());
    }

    @Test
    void findById_withExistingId_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.sku()).isEqualTo("TEST-001");
    }

    @Test
    void findById_withNonExistingId_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_returnsPagedProducts() {
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAllFiltered(any(), any(), any(), any())).thenReturn(productPage);

        var response = productService.findAll(null, null, true, PageRequest.of(0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1L);
    }

    @Test
    void toggleActive_deactivatesActiveProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        productService.toggleActive(1L, currentUser);

        assertThat(product.getActive()).isFalse();
        verify(productRepository).save(product);
    }
}
