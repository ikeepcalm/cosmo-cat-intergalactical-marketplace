package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.database.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);

    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    long countByProductId(Long productId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantityOrderedByProduct(@Param("productId") Long productId);

    @Query("SELECT oi.product.id FROM OrderItem oi GROUP BY oi.product.id ORDER BY SUM(oi.quantity) DESC")
    List<Long> findMostOrderedProducts();
}