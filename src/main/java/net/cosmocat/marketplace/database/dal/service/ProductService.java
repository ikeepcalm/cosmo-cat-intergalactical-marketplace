package net.cosmocat.marketplace.database.dal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateDTO;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateDTO;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.projection.ProductPurchaseReport;
import net.cosmocat.marketplace.database.repository.CategoryRepository;
import net.cosmocat.marketplace.database.repository.OrderItemRepository;
import net.cosmocat.marketplace.database.repository.ProductRepository;
import net.cosmocat.marketplace.exception.type.CategoryNotFoundException;
import net.cosmocat.marketplace.exception.type.ProductConflictException;
import net.cosmocat.marketplace.exception.type.ProductNotFoundException;
import net.cosmocat.marketplace.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

  private final ProductMapper productMapper;
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final OrderItemRepository orderItemRepository;

  public List<ProductDTO> getAllProducts() {
    log.info("Retrieving all products");
    List<Product> productList = productRepository.findAll();
    return productMapper.toProductDTOList(productList);
  }

  public ProductDTO getProductById(Long id) {
    log.info("Retrieving product with ID: {}", id);
    Product product = productRepository.findById(id)
        .orElseThrow(() -> ProductNotFoundException.forId(id));
    return productMapper.toProductDTO(product);
  }

  @Transactional
  public ProductDTO createProduct(ProductCreateDTO request) {
    log.info("Creating new product: {}", request.getName());

    if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
      throw ProductConflictException.forDuplicateSku(request.getSku());
    }

    Product product = productMapper.toProductEntity(request);

    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> CategoryNotFoundException.forId(request.getCategoryId()));
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    log.info("Product created successfully with ID: {}", savedProduct.getId());

    return productMapper.toProductDTO(savedProduct);
  }

  @Transactional
  public ProductDTO updateProduct(Long id, ProductUpdateDTO request) {
    log.info("Updating product with ID: {}", id);

    Product existingProduct = productRepository.findById(id)
        .orElseThrow(() -> ProductNotFoundException.forId(id));

    productMapper.updateProductEntityFromRequest(request, existingProduct);

    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> CategoryNotFoundException.forId(request.getCategoryId()));
      existingProduct.setCategory(category);
    }

    Product updatedProduct = productRepository.save(existingProduct);
    log.info("Product updated successfully with ID: {}", id);

    return productMapper.toProductDTO(updatedProduct);
  }

  @Transactional
  public void deleteProduct(Long id) {
    log.info("Deleting product with ID: {}", id);
    if (!productRepository.existsById(id)) {
      throw ProductNotFoundException.forId(id);
    }
    productRepository.deleteById(id);
  }

  public List<ProductDTO> searchProductsByName(String name) {
    log.info("Searching products by name: {}", name);
    List<Product> productList = productRepository.findByNameContainingIgnoreCase(name);
    return productMapper.toProductDTOList(productList);
  }

  public List<ProductPurchaseReport> getMostPurchasedProductsReport() {
    log.info("Generating report for most purchased products");
    return orderItemRepository.findMostPurchasedProducts();
  }
}
