package net.cosmocat.marketplace.database.dal.repository;

import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {

    private final Map<Long, Product> products = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ProductRepository() {
        initMockData();
    }

    private void initMockData() {
        Category electronics = createMockCategory(1L, "Electronics", "Electronic devices and gadgets");
        Category books = createMockCategory(2L, "Books", "Books and literature");
        Category clothing = createMockCategory(3L, "Clothing", "Apparel and fashion items");

        createMockProduct("Laptop HP", "High performance laptop", 999.99, "USD", electronics, "LAPTOP001", 10);
        createMockProduct("Smartphone Samsung", "Latest Android smartphone", 699.99, "USD", electronics, "PHONE001", 25);
        createMockProduct("Java Programming Book", "Complete guide to Java programming", 49.99, "USD", books, "BOOK001", 50);
        createMockProduct("T-Shirt Cotton", "Comfortable cotton t-shirt", 19.99, "USD", clothing, "SHIRT001", 100);
        createMockProduct("Wireless Headphones", "Bluetooth wireless headphones", 129.99, "USD", electronics, "HEAD001", 15);
    }

    private Category createMockCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setTags(Arrays.asList("popular", "featured"));
        return category;
    }

    private void createMockProduct(String name, String description, Double price, String currency,
                                   Category category, String sku, Integer stockQuantity) {
        Product product = new Product();
        Long id = idGenerator.getAndIncrement();
        product.setId(id);
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
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setImage("https://example.com/image/" + id + ".jpg");

        products.put(id, product);
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        products.put(product.getId(), product);
        return product;
    }

    public void deleteById(Long id) {
        products.remove(id);
    }

    public boolean existsById(Long id) {
        return products.containsKey(id);
    }

    public List<Product> findByCategory(Category category) {
        return products.values().stream()
                .filter(product -> Objects.equals(product.getCategory().getId(), category.getId()))
                .toList();
    }

    public List<Product> findByNameContaining(String name) {
        return products.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }
}