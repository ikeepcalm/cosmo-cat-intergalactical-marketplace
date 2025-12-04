package net.cosmocat.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.cosmocat.marketplace.config.SecurityConfig;
import net.cosmocat.marketplace.database.dal.service.ProductService;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateDTO;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateDTO;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.exception.type.ProductConflictException;
import net.cosmocat.marketplace.exception.type.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/v1/products - Should return all products")
    void getAllProductsShouldReturnAllProducts() throws Exception {
        // Given
        ProductDTO product1 = createProductDTO(1L, "Laptop HP Pro", 1299.99, "LAPTOP001");
        ProductDTO product2 = createProductDTO(2L, "Smartphone Samsung", 799.99, "PHONE001");
        List<ProductDTO> products = Arrays.asList(product1, product2);

        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop HP Pro"))
                .andExpect(jsonPath("$[0].price").value(1299.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Smartphone Samsung"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/v1/products - Should return empty list when no products exist")
    void getAllProductsShouldReturnEmptyListWhenNoProducts() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return product by ID")
    void getProductByIdShouldReturnProduct() throws Exception {
        // Given
        Long productId = 1L;
        ProductDTO product = createProductDTO(productId, "Laptop HP Pro", 1299.99, "LAPTOP001");

        when(productService.getProductById(productId)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop HP Pro"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.sku").value("LAPTOP001"));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 404 when product not found")
    void getProductByIdShouldReturn404WhenNotFound() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(productService.getProductById(nonExistentId))
                .thenThrow(new ProductNotFoundException("Product with ID 999 not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(nonExistentId);
    }

    @Test
    @DisplayName("POST /api/v1/products - Should create new product successfully")
    void createProductShouldReturnCreatedProduct() throws Exception {
        // Given
        ProductCreateDTO createRequest = new ProductCreateDTO(
                "Cosmic Mouse",
                "A cosmic gaming mouse",
                29.99,
                "USD",
                50,
                "MOUSE001",
                "https://example.com/mouse.jpg",
                0.1,
                "10 x 5 x 3 cm",
                AvailabilityStatus.AVAILABLE,
                1L
        );

        ProductDTO createdProduct = createProductDTO(10L, "Cosmic Mouse", 29.99, "MOUSE001");

        when(productService.createProduct(any(ProductCreateDTO.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Cosmic Mouse"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.sku").value("MOUSE001"));

        verify(productService, times(1)).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 for invalid request data")
    void createProductShouldReturn400ForInvalidData() throws Exception {
        // Given - Invalid product with null name
        String invalidRequest = """
                {
                    "name": null,
                    "description": "Test product",
                    "price": 29.99,
                    "currency": "USD",
                    "stockQuantity": 50,
                    "sku": "TEST001",
                    "imageUrl": "https://example.com/test.jpg",
                    "weight": 0.1,
                    "dimensions": "10x5x3 cm",
                    "availabilityStatus": "AVAILABLE",
                    "categoryId": 1
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 409 when SKU already exists")
    void createProductShouldReturn409ForDuplicateSku() throws Exception {
        // Given
        ProductCreateDTO createRequest = new ProductCreateDTO(
                "Galaxy Duplicate Product",
                "This has a duplicate SKU",
                99.99,
                "USD",
                10,
                "LAPTOP001",
                "https://example.com/duplicate.jpg",
                1.0,
                "10 x 10 x 10 cm",
                AvailabilityStatus.AVAILABLE,
                1L
        );

        when(productService.createProduct(any(ProductCreateDTO.class)))
                .thenThrow(new ProductConflictException("Product with SKU LAPTOP001 already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(productService, times(1)).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should update product successfully")
    void updateProductShouldReturnUpdatedProduct() throws Exception {
        // Given
        Long productId = 1L;
        ProductUpdateDTO updateRequest = new ProductUpdateDTO(
                "Galactic Laptop Pro Updated",
                "Updated description",
                1099.99,
                "USD",
                15,
                "LAPTOP001",
                "https://example.com/laptop-updated.jpg",
                2.0,
                "35 x 25 x 2 cm",
                AvailabilityStatus.AVAILABLE,
                1L
        );

        ProductDTO updatedProduct = createProductDTO(productId, "Galactic Laptop Pro Updated", 1099.99, "LAPTOP001");

        when(productService.updateProduct(eq(productId), any(ProductUpdateDTO.class)))
                .thenReturn(updatedProduct);

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Galactic Laptop Pro Updated"))
                .andExpect(jsonPath("$.price").value(1099.99));

        verify(productService, times(1)).updateProduct(eq(productId), any(ProductUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 404 when product not found")
    void updateProductShouldReturn404WhenNotFound() throws Exception {
        // Given
        Long nonExistentId = 999L;
        ProductUpdateDTO updateRequest = new ProductUpdateDTO(
                "Lunar Updated Name",
                "Updated Description",
                99.99,
                "USD",
                10,
                "SKU999",
                "https://example.com/updated.jpg",
                1.0,
                "10 x 10 x 10 cm",
                AvailabilityStatus.AVAILABLE,
                1L
        );

        when(productService.updateProduct(eq(nonExistentId), any(ProductUpdateDTO.class)))
                .thenThrow(new ProductNotFoundException("Product with ID 999 not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updateProduct(eq(nonExistentId), any(ProductUpdateDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 400 for invalid request data")
    void updateProductShouldReturn400ForInvalidData() throws Exception {
        // Given - Invalid product with negative price
        Long productId = 1L;
        String invalidRequest = """
                {
                    "name": "Valid Name",
                    "description": "Valid description",
                    "price": -99.99,
                    "currency": "USD",
                    "stockQuantity": 10,
                    "sku": "SKU001",
                    "imageUrl": "https://example.com/test.jpg",
                    "weight": 1.0,
                    "dimensions": "10x10x10 cm",
                    "availabilityStatus": "AVAILABLE",
                    "categoryId": 1
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(eq(productId), any(ProductUpdateDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should delete product successfully")
    void deleteProductShouldReturnNoContent() throws Exception {
        // Given
        Long productId = 1L;
        doNothing().when(productService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should return matching products")
    void searchProductsShouldReturnMatchingProducts() throws Exception {
        // Given
        String searchQuery = "Laptop";
        ProductDTO product1 = createProductDTO(1L, "Laptop HP Pro", 1299.99, "LAPTOP001");
        ProductDTO product2 = createProductDTO(2L, "Laptop Dell XPS", 1499.99, "LAPTOP002");
        List<ProductDTO> searchResults = Arrays.asList(product1, product2);

        when(productService.searchProductsByName(searchQuery)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", searchQuery))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", containsString("Laptop")))
                .andExpect(jsonPath("$[1].name", containsString("Laptop")));

        verify(productService, times(1)).searchProductsByName(searchQuery);
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Should return empty list when no matches found")
    void searchProductsShouldReturnEmptyListWhenNoMatches() throws Exception {
        // Given
        String searchQuery = "NonExistentProduct";
        when(productService.searchProductsByName(searchQuery)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", searchQuery))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productService, times(1)).searchProductsByName(searchQuery);
    }

    private ProductDTO createProductDTO(Long id, String name, Double price, String sku) {
        return new ProductDTO(
                id,
                name,
                "Test description for " + name,
                "https://google.com/image.jpg",
                price,
                "USD",
                10,
                sku,
                AvailabilityStatus.AVAILABLE,
                1.0,
                "10x10x10 cm", LocalDateTime.now(), LocalDateTime.now(),
                null
        );
    }
}
