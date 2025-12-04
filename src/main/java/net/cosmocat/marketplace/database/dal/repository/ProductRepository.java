package net.cosmocat.marketplace.database.dal.repository;

import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByAvailabilityStatus(AvailabilityStatus status);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    List<Product> findByCategoryIdAndAvailabilityStatus(Long categoryId, AvailabilityStatus status);

    @Query("SELECT p FROM products p WHERE p.stockQuantity < :quantity")
    List<Product> findLowStockProducts(@Param("quantity") Integer quantity);

    @Query("SELECT p FROM products p WHERE p.category.id = :categoryId AND p.availabilityStatus = 'AVAILABLE'")
    List<Product> findAvailableProductsByCategory(@Param("categoryId") Long categoryId);
}