package com.inventory.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.domain.enums.RoleType;
import com.inventory.management.dto.request.CreateProductRequest;
import com.inventory.management.dto.response.ProductResponse;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.service.ProductService;
import com.inventory.management.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProductService productService;
    @MockBean UserRepository userRepository;
    @MockBean com.inventory.management.security.JwtService jwtService;
    @MockBean com.inventory.management.security.UserDetailsServiceImpl userDetailsService;

    private UsernamePasswordAuthenticationToken adminAuth;
    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        adminAuth = new UsernamePasswordAuthenticationToken(
                "admin@test.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        sampleResponse = new ProductResponse(
                1L, "TEST-001", "Test Product", "desc",
                1L, "Electronics", "UNIDAD",
                new BigDecimal("100.00"), true, Instant.now()
        );
    }

    @Test
    void findById_returnsProduct() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/products/1").with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void create_withValidRequest_returns201() throws Exception {
        CreateProductRequest req = new CreateProductRequest(
                "NEW-001", "New Product", "desc", 1L, "UNIDAD", new BigDecimal("50.00"));

        when(userRepository.findById(any())).thenReturn(Optional.of(
                com.inventory.management.domain.entity.User.builder()
                        .id(1L).name("Admin").lastName("Test").email("admin@test.com")
                        .role(com.inventory.management.domain.entity.Role.builder()
                                .name(RoleType.ADMIN).build())
                        .active(true).build()
        ));
        when(productService.create(any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/products")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_withInvalidSku_returns422() throws Exception {
        CreateProductRequest req = new CreateProductRequest(
                "", "Name", null, null, "UNIDAD", BigDecimal.ONE);

        mockMvc.perform(post("/products")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void findById_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isForbidden());
    }
}
