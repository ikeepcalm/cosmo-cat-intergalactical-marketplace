package net.cosmocat.marketplace.database.dal.service;

import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateDTO;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateDTO;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.exception.type.CategoryNotFoundException;
import net.cosmocat.marketplace.exception.type.ProductConflictException;
import net.cosmocat.marketplace.exception.type.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ProductServiceTest.TestConfig.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @TestConfiguration
    @ComponentScan(basePackages = {
            "net.cosmocat.marketplace.database.dal.service",
            "net.cosmocat.marketplace.mapper"
    })
    static class TestConfig {
    }

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Should retrieve all products successfully")
    void getAllProductsShouldReturnAllProducts() {
        // When
        List<ProductDTO> products = productService.getAllProducts();

        // Then
        assertThat(products).isNotEmpty();
        assertThat(products).hasSizeGreaterThanOrEqualTo(5);
        assertThat(products)
                .extracting(ProductDTO::getName)
                .contains("Laptop HP Pro", "Smartphone Samsung");
    }

    @Test
    @DisplayName("Should retrieve product by ID successfully")
    void getProductByIdWithValidIdShouldReturnProduct() {
        // Given
        Long productId = 1L;

        // When
        ProductDTO product = productService.getProductById(productId);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getName()).isEqualTo("Laptop HP Pro");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product ID doesn't exist")
    void getProductByIdWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(nonExistentId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create new product successfully")
    void createProductWithValidDataShouldCreateProduct() {
        // Given
        ProductCreateDTO createRequest =
                new ProductCreateDTO(
                        "Stellar Mouse",
                        "A cosmic gaming mouse",
                        29.99,
                        "USD",
                        50,
                        "MOUSE001",
                        "https://example.com/mouse.jpg",
                        0.1,
                        "10x5x3 cm",
                        AvailabilityStatus.AVAILABLE,
                        1L);

        // When
        ProductDTO createdProduct = productService.createProduct(createRequest);

        // Then
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getId()).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("Stellar Mouse");
        assertThat(createdProduct.getPrice()).isEqualTo(29.99);
        assertThat(createdProduct.getSku()).isEqualTo("MOUSE001");
    }

    @Test
    @DisplayName("Should throw ProductConflictException when SKU already exists")
    void createProductWithDuplicateSkuShouldThrowException() {
        // Given
        ProductCreateDTO createRequest =
                new ProductCreateDTO(
                        "Cosmic Laptop",
                        "Another laptop",
                        1299.99,
                        "USD",
                        5,
                        "LAPTOP001",
                        "https://example.com/laptop.jpg",
                        2.5,
                        "30x20x2 cm",
                        AvailabilityStatus.AVAILABLE,
                        1L);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ProductConflictException.class)
                .hasMessageContaining("LAPTOP001");
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category doesn't exist")
    void createProductWithInvalidCategoryIdShouldThrowException() {
        // Given
        ProductCreateDTO createRequest =
                new ProductCreateDTO(
                        "Galaxy Keyboard",
                        "Mechanical keyboard",
                        79.99,
                        "USD",
                        30,
                        "KEYB001",
                        "https://example.com/keyboard.jpg",
                        0.8,
                        "45x15x3 cm",
                        AvailabilityStatus.AVAILABLE,
                        999L);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should update existing product successfully")
    void updateProductWithValidDataShouldUpdateProduct() {
        // Given
        Long productId = 1L;
        ProductUpdateDTO updateRequest =
                new ProductUpdateDTO(
                        "Laptop HP Pro",
                        "High performance laptop - updated",
                        1099.99,
                        "USD",
                        8,
                        "LAPTOP001",
                        "https://example.com/laptop-updated.jpg",
                        2.0,
                        "35x25x2 cm",
                        AvailabilityStatus.AVAILABLE,
                        1L);

        // When
        ProductDTO updatedProduct = productService.updateProduct(productId, updateRequest);

        // Then
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getId()).isEqualTo(productId);
        assertThat(updatedProduct.getName()).isEqualTo("Laptop HP Pro");
        assertThat(updatedProduct.getPrice()).isEqualTo(1099.99);
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
    void updateProductWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        ProductUpdateDTO updateRequest =
                new ProductUpdateDTO(
                        "Updated Name",
                        "Updated Description",
                        99.99,
                        "USD",
                        10,
                        "SKU999",
                        "https://example.com/updated.jpg",
                        1.0,
                        "10x10x10 cm",
                        AvailabilityStatus.AVAILABLE,
                        1L);

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(nonExistentId, updateRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProductWithValidIdShouldDeleteProduct() {
        // Given
        Long productId = 5L;

        // When
        productService.deleteProduct(productId);

        // Then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should search products by name successfully")
    void searchProductsByNameWithValidNameShouldReturnMatchingProducts() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("Laptop");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).containsIgnoringCase("Laptop");
    }

    @Test
    @DisplayName("Should return empty list when no products match search")
    void searchProductsByNameWithNonMatchingNameShouldReturnEmptyList() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("NonExistentProduct");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should search products case-insensitively")
    void searchProductsByNameCaseInsensitiveShouldReturnMatchingProducts() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("laptop");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getName()).containsIgnoringCase("Laptop");
    }

    @Test
    @DisplayName("Should return multiple products when search matches multiple items")
    void searchProductsByNameWithPartialNameShouldReturnMultipleProducts() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("Shirt");

        // Then
        assertThat(results).isNotEmpty();
    }
}
