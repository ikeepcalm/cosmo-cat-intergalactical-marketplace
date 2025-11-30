package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.entity.Order;
import net.cosmocat.marketplace.database.entity.User;
import net.cosmocat.marketplace.database.entity.source.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIT extends TestContainersBaseTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Order order1;
    private Order order2;
    private Order order3;
    private Order order4;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Users
        user1 = createUser("user1@example.com", "user1", "John", "Doe");
        user2 = createUser("user2@example.com", "user2", "Jane", "Smith");

        // Setup Orders
        order1 = createOrder(user1, "ORD001", OrderStatus.PENDING, new BigDecimal("100.00"), "USD", LocalDateTime.now().minusDays(5));
        order2 = createOrder(user1, "ORD002", OrderStatus.DELIVERED, new BigDecimal("250.50"), "USD", LocalDateTime.now().minusDays(2));
        order3 = createOrder(user2, "ORD003", OrderStatus.SHIPPED, new BigDecimal("50.00"), "USD", LocalDateTime.now().minusDays(1));
        order4 = createOrder(user1, "ORD004", OrderStatus.PENDING, new BigDecimal("120.00"), "USD", LocalDateTime.now().minusDays(10));
        createOrder(user2, "ORD005", OrderStatus.DELIVERED, new BigDecimal("300.00"), "USD", LocalDateTime.now().minusDays(3));
    }

    private User createUser(String email, String username, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash("hashedPassword123"); // Corrected: setPassword to setPasswordHash
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    private Order createOrder(User user, String orderNumber, OrderStatus status, BigDecimal totalAmount, String currency, LocalDateTime orderDate) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setCurrency(currency); // Set the currency
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("Should save a new order successfully")
    void saveNewOrder() {
        // Given
        Order newOrder = new Order();
        newOrder.setUser(user1);
        newOrder.setOrderNumber("ORD006");
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.CONFIRMED);
        newOrder.setTotalAmount(new BigDecimal("75.00"));
        newOrder.setCurrency("USD"); // Set the currency

        // When
        Order savedOrder = orderRepository.save(newOrder);

        // Then
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getOrderNumber()).isEqualTo("ORD006");
        assertThat(orderRepository.count()).isEqualTo(6); // 5 initial + 1 new
    }

    @Test
    @DisplayName("Should find order by ID")
    void findOrderById() {
        // Given
        Long orderId = order1.getId();

        // When
        Optional<Order> foundOrder = orderRepository.findById(orderId);

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD001");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent order ID")
    void findOrderByIdNotFound() {
        // When
        Optional<Order> foundOrder = orderRepository.findById(999L);

        // Then
        assertThat(foundOrder).isNotPresent();
    }

    @Test
    @DisplayName("Should find all orders")
    void findAllOrders() {
        // When
        List<Order> orders = orderRepository.findAll();

        // Then
        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSize(5);
        assertThat(orders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD001", "ORD002", "ORD003", "ORD004", "ORD005");
    }

    @Test
    @DisplayName("Should update an existing order")
    void updateOrder() {
        // Given
        Order existingOrder = order1;
        existingOrder.setStatus(OrderStatus.DELIVERED);
        existingOrder.setTotalAmount(new BigDecimal("110.00"));
        existingOrder.setCurrency("USD"); // Set the currency

        // When
        Order updatedOrder = orderRepository.save(existingOrder);

        // Then
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getId()).isEqualTo(existingOrder.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(updatedOrder.getTotalAmount()).isEqualTo(new BigDecimal("110.00"));
    }

    @Test
    @DisplayName("Should delete an order by ID")
    void deleteOrderById() {
        // Given
        Long orderId = order3.getId();

        // When
        orderRepository.deleteById(orderId);

        // Then
        assertThat(orderRepository.findById(orderId)).isNotPresent();
        assertThat(orderRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find order by order number")
    void findByOrderNumber() {
        // When
        Optional<Order> foundOrder = orderRepository.findByOrderNumber("ORD002");

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getUser().getUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent order number")
    void findByOrderNumberNotFound() {
        // When
        Optional<Order> foundOrder = orderRepository.findByOrderNumber("NONEXISTENT");

        // Then
        assertThat(foundOrder).isNotPresent();
    }

    @Test
    @DisplayName("Should check if order exists by order number")
    void existsByOrderNumber() {
        // When
        boolean exists = orderRepository.existsByOrderNumber("ORD003");
        boolean notExists = orderRepository.existsByOrderNumber("NONEXISTENT");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find orders by user ID")
    void findByUserId() {
        // When
        List<Order> user1Orders = orderRepository.findByUserId(user1.getId());
        List<Order> user2Orders = orderRepository.findByUserId(user2.getId());

        // Then
        assertThat(user1Orders).hasSize(3);
        assertThat(user1Orders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD001", "ORD002", "ORD004");
        assertThat(user2Orders).hasSize(2);
        assertThat(user2Orders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD003", "ORD005");
    }

    @Test
    @DisplayName("Should find orders by status")
    void findByStatus() {
        // When
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> deliveredOrders = orderRepository.findByStatus(OrderStatus.DELIVERED);
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);

        // Then
        assertThat(pendingOrders).hasSize(2);
        assertThat(pendingOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD001", "ORD004");
        assertThat(deliveredOrders).hasSize(2);
        assertThat(deliveredOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD002", "ORD005");
        assertThat(shippedOrders).hasSize(1);
        assertThat(shippedOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD003");
    }

    @Test
    @DisplayName("Should find orders by user ID and status")
    void findByUserIdAndStatus() {
        // When
        List<Order> user1PendingOrders = orderRepository.findByUserIdAndStatus(user1.getId(), OrderStatus.PENDING);
        List<Order> user2DeliveredOrders = orderRepository.findByUserIdAndStatus(user2.getId(), OrderStatus.DELIVERED);

        // Then
        assertThat(user1PendingOrders).hasSize(2);
        assertThat(user1PendingOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD001", "ORD004");
        assertThat(user2DeliveredOrders).hasSize(1);
        assertThat(user2DeliveredOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD005");
    }

    @Test
    @DisplayName("Should find orders by order date between a range")
    void findByOrderDateBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(4);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        // When
        List<Order> ordersInDateRange = orderRepository.findByOrderDateBetween(start, end);

        // Then
        assertThat(ordersInDateRange).hasSize(2);
        assertThat(ordersInDateRange).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD002", "ORD003");
    }

    @Test
    @DisplayName("Should find orders by total amount greater than")
    void findByTotalAmountGreaterThan() {
        // Given
        BigDecimal amountThreshold = new BigDecimal("100.00");

        // When
        List<Order> expensiveOrders = orderRepository.findByTotalAmountGreaterThan(amountThreshold);

        // Then
        assertThat(expensiveOrders).hasSize(3);
        assertThat(expensiveOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD002", "ORD004", "ORD005");
    }

    @Test
    @DisplayName("Should find recent orders by user, ordered by date descending")
    void findRecentOrdersByUser() {
        // When
        List<Order> user1RecentOrders = orderRepository.findRecentOrdersByUser(user1.getId());

        // Then
        assertThat(user1RecentOrders).hasSize(3);
        assertThat(user1RecentOrders.get(0).getOrderNumber()).isEqualTo("ORD002"); // Most recent for user1
        assertThat(user1RecentOrders.get(1).getOrderNumber()).isEqualTo("ORD001");
        assertThat(user1RecentOrders.get(2).getOrderNumber()).isEqualTo("ORD004");
    }

    @Test
    @DisplayName("Should find old pending orders")
    void findOldPendingOrders() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(3); // Orders older than 3 days

        // When
        List<Order> oldPendingOrders = orderRepository.findOldPendingOrders(cutoffDate);

        // Then
        assertThat(oldPendingOrders).hasSize(2);
        assertThat(oldPendingOrders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD001", "ORD004");
    }

    @Test
    @DisplayName("Should calculate total revenue by user for delivered orders")
    void calculateTotalRevenueByUser() {
        // When
        BigDecimal user1Revenue = orderRepository.calculateTotalRevenueByUser(user1.getId());
        BigDecimal user2Revenue = orderRepository.calculateTotalRevenueByUser(user2.getId());
        BigDecimal nonExistentUserRevenue = orderRepository.calculateTotalRevenueByUser(999L);

        // Then
        assertThat(user1Revenue).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(user2Revenue).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(nonExistentUserRevenue).isNull();
    }

    @Test
    @DisplayName("Should find orders by user ID ordered by order date descending")
    void findByUserIdOrderByOrderDateDesc() {
        // When
        List<Order> user1OrdersSorted = orderRepository.findByUserIdOrderByOrderDateDesc(user1.getId());

        // Then
        assertThat(user1OrdersSorted).hasSize(3);
        assertThat(user1OrdersSorted.get(0).getOrderNumber()).isEqualTo("ORD002");
        assertThat(user1OrdersSorted.get(1).getOrderNumber()).isEqualTo("ORD001");
        assertThat(user1OrdersSorted.get(2).getOrderNumber()).isEqualTo("ORD004");
    }

    @Test
    @DisplayName("Should count orders by status")
    void countByStatus() {
        // When
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long deliveredCount = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long shippedCount = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Then
        assertThat(pendingCount).isEqualTo(2);
        assertThat(deliveredCount).isEqualTo(2);
        assertThat(shippedCount).isEqualTo(1);
        assertThat(cancelledCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving order with duplicate order number")
    void saveOrderWithDuplicateOrderNumberShouldThrowException() {
        // Given
        Order duplicateOrder = new Order();
        duplicateOrder.setUser(user1);
        duplicateOrder.setOrderNumber("ORD001"); // Duplicate order number
        duplicateOrder.setOrderDate(LocalDateTime.now());
        duplicateOrder.setStatus(OrderStatus.CONFIRMED);
        duplicateOrder.setTotalAmount(new BigDecimal("99.99"));
        duplicateOrder.setCurrency("USD"); // Set the currency

        // When & Then
        assertThatThrownBy(() -> orderRepository.save(duplicateOrder))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
