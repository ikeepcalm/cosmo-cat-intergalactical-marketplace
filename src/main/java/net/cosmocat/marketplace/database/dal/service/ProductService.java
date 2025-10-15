package net.cosmocat.marketplace.database.dal.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateDTO;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateDTO;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.exception.CategoryNotFoundException;
import net.cosmocat.marketplace.exception.ProductConflictException;
import net.cosmocat.marketplace.exception.ProductNotFoundException;
import net.cosmocat.marketplace.mapper.ProductMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductService {

  private final ProductMapper productMapper;
  private final Map<Long, Product> products = new HashMap<>();
  private final Map<Long, Category> categories = new HashMap<>();
  private final AtomicLong productIdGenerator = new AtomicLong(1);

  public ProductService(ProductMapper productMapper) {
    this.productMapper = productMapper;
    initMockData();
  }

  private void initMockData() {
    // Initialize categories
    Category electronics = createMockCategory(1L, "Electronics", "Electronic devices and gadgets");
    Category books = createMockCategory(2L, "Books", "Books and literature");
    Category clothing = createMockCategory(3L, "Clothing", "Apparel and fashion items");

    // Initialize products
    createMockProduct(
        "Laptop HP", "High performance laptop", 999.99, "USD", electronics, "LAPTOP001", 10);
    createMockProduct(
        "Smartphone Samsung",
        "Latest Android smartphone",
        699.99,
        "USD",
        electronics,
        "PHONE001",
        25);
    createMockProduct(
        "Java Programming Book",
        "Complete guide to Java programming",
        49.99,
        "USD",
        books,
        "BOOK001",
        50);
    createMockProduct(
        "T-Shirt Cotton", "Comfortable cotton t-shirt", 19.99, "USD", clothing, "SHIRT001", 100);
    createMockProduct(
        "Wireless Headphones",
        "Bluetooth wireless headphones",
        129.99,
        "USD",
        electronics,
        "HEAD001",
        15);
  }

  private Category createMockCategory(Long id, String name, String description) {
    Category category = new Category();
    category.setId(id);
    category.setName(name);
    category.setDescription(description);
    category.setTags(Arrays.asList("popular", "featured"));
    categories.put(id, category);
    return category;
  }

  private void createMockProduct(
      String name,
      String description,
      Double price,
      String currency,
      Category category,
      String sku,
      Integer stockQuantity) {
    Product product = new Product();
    Long id = productIdGenerator.getAndIncrement();
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

  public List<ProductDTO> getAllProducts() {
    log.info("Retrieving all products");
    List<Product> productList = new ArrayList<>(products.values());
    return productMapper.toDTOList(productList);
  }

  public ProductDTO getProductById(Long id) {
    log.info("Retrieving product with ID: {}", id);
    Product product = products.get(id);
    if (product == null) {
      throw ProductNotFoundException.forId(id);
    }
    return productMapper.toDTO(product);
  }

  public ProductDTO createProduct(ProductCreateDTO request) {
    log.info("Creating new product: {}", request.getName());

    if (request.getSku() != null) {
      boolean skuExists =
          products.values().stream().anyMatch(p -> request.getSku().equals(p.getSku()));
      if (skuExists) {
        throw ProductConflictException.forDuplicateSku(request.getSku());
      }
    }

    Product product = productMapper.toEntity(request);
    Long id = productIdGenerator.getAndIncrement();
    product.setId(id);
    product.setCreatedAt(LocalDateTime.now());
    product.setUpdatedAt(LocalDateTime.now());

    if (request.getCategoryId() != null) {
      Category category = categories.get(request.getCategoryId());
      if (category != null) {
        product.setCategory(category);
      } else {
        throw CategoryNotFoundException.forId(request.getCategoryId());
      }
    }

    products.put(id, product);
    log.info("Product created successfully with ID: {}", id);

    return productMapper.toDTO(product);
  }

  public ProductDTO updateProduct(Long id, ProductUpdateDTO request) {
    log.info("Updating product with ID: {}", id);

    Product existingProduct = products.get(id);
    if (existingProduct == null) {
      throw ProductNotFoundException.forId(id);
    }

    productMapper.updateEntityFromRequest(request, existingProduct);

    if (request.getCategoryId() != null) {
      Category category = categories.get(request.getCategoryId());
      if (category != null) {
        existingProduct.setCategory(category);
      } else {
        throw CategoryNotFoundException.forId(request.getCategoryId());
      }
    }

    existingProduct.setUpdatedAt(LocalDateTime.now());
    products.put(id, existingProduct);
    log.info("Product updated successfully with ID: {}", id);

    return productMapper.toDTO(existingProduct);
  }

  public void deleteProduct(Long id) {
    log.info("Deleting product with ID: {}", id);
    products.remove(id);
  }

  public List<ProductDTO> searchProductsByName(String name) {
    log.info("Searching products by name: {}", name);
    List<Product> productList =
        products.values().stream()
            .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
            .toList();
    return productMapper.toDTOList(productList);
  }
}
