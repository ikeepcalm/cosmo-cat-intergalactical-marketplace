package net.cosmocat.marketplace.database.dal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.database.entity.Order;
import net.cosmocat.marketplace.database.entity.OrderItem;
import net.cosmocat.marketplace.database.entity.source.OrderStatus;
import net.cosmocat.marketplace.database.repository.OrderItemRepository;
import net.cosmocat.marketplace.database.repository.OrderRepository;
import net.cosmocat.marketplace.database.repository.ProductRepository;
import net.cosmocat.marketplace.database.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<Order> getAllOrders() {
        log.info("Retrieving all orders");
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        log.info("Retrieving order with ID: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }

    public Order getOrderByOrderNumber(String orderNumber) {
        log.info("Retrieving order with order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        log.info("Retrieving orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getRecentOrdersByUser(Long userId) {
        log.info("Retrieving recent orders for user ID: {}", userId);
        return orderRepository.findRecentOrdersByUser(userId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.info("Retrieving orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByUserAndStatus(Long userId, OrderStatus status) {
        log.info("Retrieving orders for user ID: {} with status: {}", userId, status);
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Retrieving orders between {} and {}", startDate, endDate);
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating new order for user ID: {}", order.getUser().getId());

        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} and order number: {}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order ID: {} to status: {}", orderId, newStatus);

        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.SHIPPED && order.getShippedDate() == null) {
            order.setShippedDate(LocalDateTime.now());
        } else if (newStatus == OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
            order.setDeliveredDate(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order ID: {} status updated from {} to {}", orderId, oldStatus, newStatus);

        return updatedOrder;
    }

    @Transactional
    public Order updateOrder(Long orderId, Order updatedOrder) {
        log.info("Updating order with ID: {}", orderId);

        Order existingOrder = getOrderById(orderId);

        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
        existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
        existingOrder.setShippedDate(updatedOrder.getShippedDate());
        existingOrder.setDeliveredDate(updatedOrder.getDeliveredDate());

        Order savedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with ID: {}", orderId);

        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);

        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        orderRepository.deleteById(orderId);
        log.info("Order deleted successfully with ID: {}", orderId);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        log.info("Retrieving order items for order ID: {}", orderId);
        return orderItemRepository.findByOrderId(orderId);
    }

    public BigDecimal calculateTotalRevenueByUser(Long userId) {
        log.info("Calculating total revenue for user ID: {}", userId);
        BigDecimal revenue = orderRepository.calculateTotalRevenueByUser(userId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public long countOrdersByStatus(OrderStatus status) {
        log.info("Counting orders with status: {}", status);
        return orderRepository.countByStatus(status);
    }

    public List<Order> findOldPendingOrders(LocalDateTime cutoffDate) {
        log.info("Finding pending orders older than: {}", cutoffDate);
        return orderRepository.findOldPendingOrders(cutoffDate);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}