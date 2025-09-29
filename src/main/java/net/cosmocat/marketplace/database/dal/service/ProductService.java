package net.cosmocat.marketplace.database.dal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.database.dal.repository.CategoryRepository;
import net.cosmocat.marketplace.database.dal.repository.ProductRepository;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateRequest;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateRequest;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.exception.ResourceNotFoundException;
import net.cosmocat.marketplace.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public List<ProductDTO> getAllProducts() {
        log.debug("Retrieving all products");
        List<Product> products = productRepository.findAll();
        return productMapper.toDTOList(products);
    }

    public Optional<ProductDTO> getProductById(Long id) {
        log.debug("Retrieving product with ID: {}", id);
        return productRepository.findById(id)
                .map(productMapper::toDTO);
    }

    public ProductDTO createProduct(ProductCreateRequest request) {
        log.debug("Creating new product: {}", request.getName());

        Product product = productMapper.toEntity(request);

        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(request.getCategoryId());
            if (category.isPresent()) {
                product.setCategory(category.get());
            } else {
                throw ResourceNotFoundException.forId("Category", request.getCategoryId());
            }
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toDTO(savedProduct);
    }

    public Optional<ProductDTO> updateProduct(Long id, ProductUpdateRequest request) {
        log.debug("Updating product with ID: {}", id);

        Optional<Product> existingProductOpt = productRepository.findById(id);
        if (existingProductOpt.isEmpty()) {
            log.warn("Product not found with ID: {}", id);
            return Optional.empty();
        }

        Product existingProduct = existingProductOpt.get();
        productMapper.updateEntityFromRequest(request, existingProduct);

        if (request.getCategoryId() != null) {
            Optional<Category> category = categoryRepository.findById(request.getCategoryId());
            if (category.isPresent()) {
                existingProduct.setCategory(category.get());
            } else {
                throw ResourceNotFoundException.forId("Category", request.getCategoryId());
            }
        }

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return Optional.of(productMapper.toDTO(updatedProduct));
    }

    public boolean deleteProduct(Long id) {
        log.debug("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            log.warn("Product not found with ID: {}", id);
            return false;
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
        return true;
    }

    public List<ProductDTO> searchProductsByName(String name) {
        log.debug("Searching products by name: {}", name);
        List<Product> products = productRepository.findByNameContaining(name);
        return productMapper.toDTOList(products);
    }
}