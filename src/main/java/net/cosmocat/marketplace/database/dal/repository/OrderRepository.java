package net.cosmocat.marketplace.database.dal.repository;

import net.cosmocat.marketplace.database.entity.Order;
import net.cosmocat.marketplace.database.entity.source.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByTotalAmountGreaterThan(BigDecimal amount);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.orderDate < :date")
    List<Order> findOldPendingOrders(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenueByUser(@Param("userId") Long userId);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    long countByStatus(OrderStatus status);
}