package net.cosmocat.marketplace.database.dal.service;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateDTO;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateDTO;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.database.dal.repository.CategoryRepository;
import net.cosmocat.marketplace.database.dal.repository.ProductRepository;
import net.cosmocat.marketplace.exception.type.CategoryNotFoundException;
import net.cosmocat.marketplace.exception.type.ProductConflictException;
import net.cosmocat.marketplace.exception.type.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductService Tests")
class ProductServiceTest extends TestContainersBaseTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronicsCategory;
    private Category booksCategory;
    private Category clothingCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronicsCategory = createCategory("Electronics", "Electronic devices and gadgets");
        booksCategory = createCategory("Books", "Books and literature");
        clothingCategory = createCategory("Clothing", "Apparel and fashion items");

        createProduct("Laptop HP Pro", "High performance laptop", 999.99, "USD", electronicsCategory, "LAPTOP001", 10);
        createProduct("Smartphone Samsung", "Latest Android smartphone", 699.99, "USD", electronicsCategory, "PHONE001", 25);
        createProduct("Java Programming Book", "Complete guide to Java programming", 49.99, "USD", booksCategory, "BOOK001", 50);
        createProduct("T-Shirt Cotton", "Comfortable cotton t-shirt", 19.99, "USD", clothingCategory, "SHIRT001", 100);
        createProduct("Wireless Headphones", "Bluetooth wireless headphones", 129.99, "USD", electronicsCategory, "HEAD001", 15);
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setTags(Arrays.asList("popular", "featured"));
        return categoryRepository.save(category);
    }

    private Product createProduct(String name, String description, Double price, String currency,
                                  Category category, String sku, Integer stockQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCurrency(currency);
        product.setCategory(category);
        product.setSku(sku);
        product.setStockQuantity(stockQuantity);
        product.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        product.setWeight(1.0);
        product.setDimensions("10x10x5 cm");
        product.setImage("https://example.com/image.jpg");
        return productRepository.save(product);
    }

    @Test
    @DisplayName("Should retrieve all products successfully")
    @Transactional
    void getAllProductsShouldReturnAllProducts() {
        // When
        List<ProductDTO> products = productService.getAllProducts();

        // Then
        assertThat(products).isNotEmpty();
        assertThat(products).hasSizeGreaterThanOrEqualTo(5);
        assertThat(products)
                .extracting(ProductDTO::name)
                .contains("Laptop HP Pro", "Smartphone Samsung");
    }

    @Test
    @DisplayName("Should retrieve product by ID successfully")
    @Transactional
    void getProductByIdWithValidIdShouldReturnProduct() {
        // Given
        Product savedProduct = productRepository.findBySku("LAPTOP001").orElseThrow();
        Long productId = savedProduct.getId();

        // When
        ProductDTO product = productService.getProductById(productId);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.id()).isEqualTo(productId);
        assertThat(product.name()).isEqualTo("Laptop HP Pro");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product ID doesn't exist")
    @Transactional
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
    @Transactional
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
                        electronicsCategory.getId());

        // When
        ProductDTO createdProduct = productService.createProduct(createRequest);

        // Then
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.id()).isNotNull();
        assertThat(createdProduct.name()).isEqualTo("Stellar Mouse");
        assertThat(createdProduct.price()).isEqualTo(29.99);
        assertThat(createdProduct.sku()).isEqualTo("MOUSE001");
    }

    @Test
    @DisplayName("Should throw ProductConflictException when SKU already exists")
    @Transactional
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
                        electronicsCategory.getId());

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ProductConflictException.class)
                .hasMessageContaining("LAPTOP001");
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category doesn't exist")
    @Transactional
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
    @Transactional
    void updateProductWithValidDataShouldUpdateProduct() {
        // Given
        Product savedProduct = productRepository.findBySku("LAPTOP001").orElseThrow();
        Long productId = savedProduct.getId();
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
                        electronicsCategory.getId());

        // When
        ProductDTO updatedProduct = productService.updateProduct(productId, updateRequest);

        // Then
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.id()).isEqualTo(productId);
        assertThat(updatedProduct.name()).isEqualTo("Laptop HP Pro");
        assertThat(updatedProduct.price()).isEqualTo(1099.99);
        assertThat(updatedProduct.stockQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
    @Transactional
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
                        electronicsCategory.getId());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(nonExistentId, updateRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should delete product successfully")
    @Transactional
    void deleteProductWithValidIdShouldDeleteProduct() {
        // Given
        Product savedProduct = productRepository.findBySku("HEAD001").orElseThrow();
        Long productId = savedProduct.getId();

        // When
        productService.deleteProduct(productId);

        // Then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should search products by name successfully")
    @Transactional
    void searchProductsByNameWithValidNameShouldReturnMatchingProducts() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("Laptop");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).containsIgnoringCase("Laptop");
    }

    @Test
    @DisplayName("Should return empty list when no products match search")
    @Transactional
    void searchProductsByNameWithNonMatchingNameShouldReturnEmptyList() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("NonExistentProduct");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should search products case-insensitively")
    @Transactional
    void searchProductsByNameCaseInsensitiveShouldReturnMatchingProducts() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("laptop");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().name()).containsIgnoringCase("Laptop");
    }

    @Test
    @DisplayName("Should return multiple products when search matches multiple items")
    @Transactional
    void searchProductsByNameWithPartialNameShouldReturnMultipleProducts() {
        // When
        List<ProductDTO> results = productService.searchProductsByName("Shirt");

        // Then
        assertThat(results).isNotEmpty();
    }
}
