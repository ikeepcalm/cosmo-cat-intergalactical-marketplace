package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.dal.repository.CategoryRepository;
import net.cosmocat.marketplace.database.dal.repository.ProductRepository;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryIT extends TestContainersBaseTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronicsCategory;
    private Category booksCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronicsCategory = createCategory("Electronics", "Electronic devices and gadgets");
        booksCategory = createCategory("Books", "Books and literature");

        createProduct("Laptop HP Pro", "High performance laptop", 999.99, "USD", electronicsCategory, "LAPTOP001", 10);
        createProduct("Smartphone Samsung", "Latest Android smartphone", 699.99, "USD", electronicsCategory, "PHONE001", 25);
        createProduct("Java Programming Book", "Complete guide to Java programming", 49.99, "USD", booksCategory, "BOOK001", 50);
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
    @DisplayName("Should save a new product successfully")
    void saveNewProduct() {
        // Given
        Product newProduct = new Product();
        newProduct.setName("New Gadget");
        newProduct.setDescription("A brand new electronic gadget");
        newProduct.setPrice(150.00);
        newProduct.setCurrency("USD");
        newProduct.setCategory(electronicsCategory);
        newProduct.setSku("GADGET001");
        newProduct.setStockQuantity(50);
        newProduct.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        newProduct.setWeight(0.5);
        newProduct.setDimensions("5x5x5 cm");
        newProduct.setImage("https://example.com/gadget.jpg");

        // When
        Product savedProduct = productRepository.save(newProduct);

        // Then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("New Gadget");
        assertThat(savedProduct.getSku()).isEqualTo("GADGET001");
        assertThat(productRepository.count()).isEqualTo(4); // 3 initial + 1 new
    }

    @Test
    @DisplayName("Should find product by ID")
    void findProductById() {
        // Given
        Product existingProduct = productRepository.findBySku("LAPTOP001").orElseThrow();

        // When
        Optional<Product> foundProduct = productRepository.findById(existingProduct.getId());

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop HP Pro");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent ID")
    void findProductByIdNotFound() {
        // When
        Optional<Product> foundProduct = productRepository.findById(999L);

        // Then
        assertThat(foundProduct).isNotPresent();
    }

    @Test
    @DisplayName("Should find all products")
    void findAllProducts() {
        // When
        List<Product> products = productRepository.findAll();

        // Then
        assertThat(products).isNotEmpty();
        assertThat(products).hasSize(3);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung", "Java Programming Book");
    }

    @Test
    @DisplayName("Should update an existing product")
    void updateProduct() {
        // Given
        Product existingProduct = productRepository.findBySku("PHONE001").orElseThrow();
        existingProduct.setPrice(750.00);
        existingProduct.setStockQuantity(30);

        // When
        Product updatedProduct = productRepository.save(existingProduct);

        // Then
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getId()).isEqualTo(existingProduct.getId());
        assertThat(updatedProduct.getPrice()).isEqualTo(750.00);
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should delete a product by ID")
    void deleteProductById() {
        // Given
        Product productToDelete = productRepository.findBySku("BOOK001").orElseThrow();
        Long productId = productToDelete.getId();

        // When
        productRepository.deleteById(productId);

        // Then
        assertThat(productRepository.findById(productId)).isNotPresent();
        assertThat(productRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find product by SKU")
    void findBySku() {
        // When
        Optional<Product> foundProduct = productRepository.findBySku("LAPTOP001");

        // Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop HP Pro");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent SKU")
    void findBySkuNotFound() {
        // When
        Optional<Product> foundProduct = productRepository.findBySku("NONEXISTENT");

        // Then
        assertThat(foundProduct).isNotPresent();
    }

    @Test
    @DisplayName("Should check if product exists by SKU")
    void existsBySku() {
        // When
        boolean exists = productRepository.existsBySku("PHONE001");
        boolean notExists = productRepository.existsBySku("NONEXISTENT");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find products by category ID")
    void findByCategoryId() {
        // When
        List<Product> electronicsProducts = productRepository.findByCategoryId(electronicsCategory.getId());
        List<Product> booksProducts = productRepository.findByCategoryId(booksCategory.getId());

        // Then
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung");
        assertThat(booksProducts).hasSize(1);
        assertThat(booksProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Java Programming Book");
    }

    @Test
    @DisplayName("Should find products by availability status")
    void findByAvailabilityStatus() {
        // Given
        Product outOfStockProduct = new Product();
        outOfStockProduct.setName("Out of Stock Item");
        outOfStockProduct.setDescription("Item that is currently unavailable");
        outOfStockProduct.setPrice(10.00);
        outOfStockProduct.setCurrency("USD");
        outOfStockProduct.setCategory(electronicsCategory);
        outOfStockProduct.setSku("OOS001");
        outOfStockProduct.setStockQuantity(0);
        outOfStockProduct.setAvailabilityStatus(AvailabilityStatus.OUT_OF_STOCK);
        productRepository.save(outOfStockProduct);

        // When
        List<Product> availableProducts = productRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        List<Product> outOfStockProducts = productRepository.findByAvailabilityStatus(AvailabilityStatus.OUT_OF_STOCK);

        // Then
        assertThat(availableProducts).hasSize(3);
        assertThat(outOfStockProducts).hasSize(1);
        assertThat(outOfStockProducts.getFirst().getName()).isEqualTo("Out of Stock Item");
    }

    @Test
    @DisplayName("Should find products by name containing ignore case")
    void findByNameContainingIgnoreCase() {
        // When
        List<Product> laptopProducts = productRepository.findByNameContainingIgnoreCase("laptop");
        List<Product> javaProducts = productRepository.findByNameContainingIgnoreCase("JAVA");
        List<Product> samsungProducts = productRepository.findByNameContainingIgnoreCase("smartphone samsung");

        // Then
        assertThat(laptopProducts).hasSize(1);
        assertThat(laptopProducts.getFirst().getName()).isEqualTo("Laptop HP Pro");
        assertThat(javaProducts).hasSize(1);
        assertThat(javaProducts.getFirst().getName()).isEqualTo("Java Programming Book");
        assertThat(samsungProducts).hasSize(1);
        assertThat(samsungProducts.getFirst().getName()).isEqualTo("Smartphone Samsung");
    }

    @Test
    @DisplayName("Should find products by price between a range")
    void findByPriceBetween() {
        // When
        List<Product> affordableProducts = productRepository.findByPriceBetween(10.00, 100.00);
        List<Product> expensiveProducts = productRepository.findByPriceBetween(500.00, 1000.00);

        // Then
        assertThat(affordableProducts).hasSize(1);
        assertThat(affordableProducts.getFirst().getName()).isEqualTo("Java Programming Book");
        assertThat(expensiveProducts).hasSize(2);
        assertThat(expensiveProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung");
    }

    @Test
    @DisplayName("Should find products by category ID and availability status")
    void findByCategoryIdAndAvailabilityStatus() {
        // Given
        Product anotherAvailableElectronics = createProduct("Smartwatch", "Wearable tech", 250.00, "USD", electronicsCategory, "WATCH001", 20);
        Product outOfStockElectronics = new Product();
        outOfStockElectronics.setName("Broken TV");
        outOfStockElectronics.setDescription("TV that is broken");
        outOfStockElectronics.setPrice(500.00);
        outOfStockElectronics.setCurrency("USD");
        outOfStockElectronics.setCategory(electronicsCategory);
        outOfStockElectronics.setSku("TV001");
        outOfStockElectronics.setStockQuantity(0);
        outOfStockElectronics.setAvailabilityStatus(AvailabilityStatus.OUT_OF_STOCK);
        productRepository.save(outOfStockElectronics);

        // When
        List<Product> availableElectronics = productRepository.findByCategoryIdAndAvailabilityStatus(electronicsCategory.getId(), AvailabilityStatus.AVAILABLE);
        List<Product> availableBooks = productRepository.findByCategoryIdAndAvailabilityStatus(booksCategory.getId(), AvailabilityStatus.AVAILABLE);

        // Then
        assertThat(availableElectronics).hasSize(3); // Laptop, Smartphone, Smartwatch
        assertThat(availableElectronics).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung", "Smartwatch");
        assertThat(availableBooks).hasSize(1);
        assertThat(availableBooks.getFirst().getName()).isEqualTo("Java Programming Book");
    }

    @Test
    @DisplayName("Should find low stock products")
    void findLowStockProducts() {
        // Given
        createProduct("Low Stock Item", "Almost out", 5.00, "USD", booksCategory, "LOW001", 2);
        createProduct("Another Low Stock", "Running out", 15.00, "USD", electronicsCategory, "LOW002", 5);

        // When
        List<Product> lowStock = productRepository.findLowStockProducts(6); // Products with stock < 6

        // Then
        assertThat(lowStock).hasSize(2);
        assertThat(lowStock).extracting(Product::getName)
                .containsExactlyInAnyOrder("Low Stock Item", "Another Low Stock");
    }

    @Test
    @DisplayName("Should find available products by category")
    void findAvailableProductsByCategory() {
        // Given
        Product outOfStockElectronics = new Product();
        outOfStockElectronics.setName("Broken Tablet");
        outOfStockElectronics.setDescription("Tablet that is broken");
        outOfStockElectronics.setPrice(300.00);
        outOfStockElectronics.setCurrency("USD");
        outOfStockElectronics.setCategory(electronicsCategory);
        outOfStockElectronics.setSku("TAB001");
        outOfStockElectronics.setStockQuantity(0);
        outOfStockElectronics.setAvailabilityStatus(AvailabilityStatus.OUT_OF_STOCK);
        productRepository.save(outOfStockElectronics);

        // When
        List<Product> availableElectronics = productRepository.findAvailableProductsByCategory(electronicsCategory.getId());
        List<Product> availableBooks = productRepository.findAvailableProductsByCategory(booksCategory.getId());

        // Then
        assertThat(availableElectronics).hasSize(2); // Laptop, Smartphone
        assertThat(availableElectronics).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung");
        assertThat(availableBooks).hasSize(1);
        assertThat(availableBooks.getFirst().getName()).isEqualTo("Java Programming Book");
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving product with duplicate SKU")
    void saveProductWithDuplicateSkuShouldThrowException() {
        // Given
        Product duplicateSkuProduct = new Product();
        duplicateSkuProduct.setName("Duplicate SKU Product");
        duplicateSkuProduct.setDescription("This product has a duplicate SKU");
        duplicateSkuProduct.setPrice(100.00);
        duplicateSkuProduct.setCurrency("USD");
        duplicateSkuProduct.setCategory(electronicsCategory);
        duplicateSkuProduct.setSku("LAPTOP001"); // Duplicate SKU
        duplicateSkuProduct.setStockQuantity(10);
        duplicateSkuProduct.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        duplicateSkuProduct.setWeight(1.0);
        duplicateSkuProduct.setDimensions("10x10x5 cm");
        duplicateSkuProduct.setImage("https://example.com/duplicate.jpg");

        // When & Then
        assertThatThrownBy(() -> productRepository.save(duplicateSkuProduct))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
