package net.cosmocat.marketplace.database.dal.service;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.dal.repository.*;
import net.cosmocat.marketplace.database.entity.*;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.database.entity.source.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderService Tests")
class OrderServiceTest extends TestContainersBaseTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testUser1;
    private User testUser2;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser1 = createUser("stellar.user@galaxy.com", "Stellar User");
        testUser2 = createUser("cosmic.buyer@universe.com", "Cosmic Buyer");

        Category testCategory = createCategory("Stellar Electronics", "Electronics from across the cosmos");
        testProduct1 = createProduct("Cosmic Laptop", "LAPTOP-001", 999.99, testCategory);
        testProduct2 = createProduct("Stellar Phone", "PHONE-001", 599.99, testCategory);

        createOrder(testUser1, "ORD-001", OrderStatus.PENDING, BigDecimal.valueOf(999.99), LocalDateTime.now().minusDays(1));
        createOrder(testUser1, "ORD-002", OrderStatus.SHIPPED, BigDecimal.valueOf(599.99), LocalDateTime.now().minusDays(2));
        createOrder(testUser2, "ORD-003", OrderStatus.DELIVERED, BigDecimal.valueOf(1599.98), LocalDateTime.now().minusDays(5));
    }

    private User createUser(String email, String username) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash("hashed_password");
        return userRepository.save(user);
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setTags(new ArrayList<>(Arrays.asList("cosmic", "stellar")));
        return categoryRepository.save(category);
    }

    private Product createProduct(String name, String sku, Double price, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(price);
        product.setCurrency("USD");
        product.setCategory(category);
        product.setSku(sku);
        product.setStockQuantity(100);
        product.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        product.setWeight(1.0);
        product.setDimensions("10x10x10 cm");
        product.setImage("https://example.com/image.jpg");
        return productRepository.save(product);
    }

    private Order createOrder(User user, String orderNumber, OrderStatus status, BigDecimal totalAmount, LocalDateTime orderDate) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setCurrency("USD");
        order.setOrderDate(orderDate);
        order.setShippingAddress("123 Cosmic Street, Galaxy City");

        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            order.setShippedDate(orderDate.plusDays(1));
        }
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredDate(orderDate.plusDays(3));
        }

        return orderRepository.save(order);
    }

    private OrderItem createOrderItem(Order order, Product product, int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setCurrency("USD");
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return orderItemRepository.save(item);
    }

    @Test
    @DisplayName("Should retrieve all orders successfully")
    @Transactional
    void getAllOrdersShouldReturnAllOrders() {
        // When
        List<Order> orders = orderService.getAllOrders();

        // Then
        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSize(3);
    }

    @Test
    @DisplayName("Should retrieve order by ID successfully")
    @Transactional
    void getOrderByIdWithValidIdShouldReturnOrder() {
        // Given
        Order savedOrder = orderRepository.findByOrderNumber("ORD-001").orElseThrow();
        Long orderId = savedOrder.getId();

        // When
        Order order = orderService.getOrderById(orderId);

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    @DisplayName("Should throw RuntimeException when order ID doesn't exist")
    @Transactional
    void getOrderByIdWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found with ID: 999");
    }

    @Test
    @DisplayName("Should retrieve order by order number successfully")
    @Transactional
    void getOrderByOrderNumberWithValidNumberShouldReturnOrder() {
        // When
        Order order = orderService.getOrderByOrderNumber("ORD-002");

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getOrderNumber()).isEqualTo("ORD-002");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Should throw RuntimeException when order number doesn't exist")
    @Transactional
    void getOrderByOrderNumberWithInvalidNumberShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> orderService.getOrderByOrderNumber("ORD-999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found with order number: ORD-999");
    }

    @Test
    @DisplayName("Should retrieve orders by user ID successfully")
    @Transactional
    void getOrdersByUserIdShouldReturnUserOrders() {
        // When
        List<Order> orders = orderService.getOrdersByUserId(testUser1.getId());

        // Then
        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(order -> order.getUser().getId().equals(testUser1.getId()));
    }

    @Test
    @DisplayName("Should retrieve recent orders by user successfully")
    @Transactional
    void getRecentOrdersByUserShouldReturnRecentOrders() {
        // When
        List<Order> orders = orderService.getRecentOrdersByUser(testUser1.getId());

        // Then
        assertThat(orders).isNotEmpty();
        assertThat(orders).allMatch(order -> order.getUser().getId().equals(testUser1.getId()));
    }

    @Test
    @DisplayName("Should retrieve orders by status successfully")
    @Transactional
    void getOrdersByStatusShouldReturnMatchingOrders() {
        // When
        List<Order> pendingOrders = orderService.getOrdersByStatus(OrderStatus.PENDING);
        List<Order> shippedOrders = orderService.getOrdersByStatus(OrderStatus.SHIPPED);

        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.getFirst().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(shippedOrders).hasSize(1);
        assertThat(shippedOrders.getFirst().getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Should retrieve orders by user and status successfully")
    @Transactional
    void getOrdersByUserAndStatusShouldReturnMatchingOrders() {
        // When
        List<Order> orders = orderService.getOrdersByUserAndStatus(testUser1.getId(), OrderStatus.PENDING);

        // Then
        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getUser().getId()).isEqualTo(testUser1.getId());
        assertThat(orders.getFirst().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should retrieve orders by date range successfully")
    @Transactional
    void getOrdersByDateRangeShouldReturnOrdersInRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(3);
        LocalDateTime endDate = LocalDateTime.now();

        // When
        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);

        // Then
        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(order ->
                !order.getOrderDate().isBefore(startDate) && !order.getOrderDate().isAfter(endDate)
        );
    }

    @Test
    @DisplayName("Should create new order successfully")
    @Transactional
    void createOrderWithValidDataShouldCreateOrder() {
        // Given
        Order newOrder = new Order();
        newOrder.setUser(testUser1);
        newOrder.setTotalAmount(BigDecimal.valueOf(1299.99));
        newOrder.setCurrency("USD");
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setShippingAddress("456 Stellar Avenue, Nebula City");

        // When
        Order createdOrder = orderService.createOrder(newOrder);

        // Then
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getOrderNumber()).isNotNull();
        assertThat(createdOrder.getOrderNumber()).startsWith("ORD-");
        assertThat(createdOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1299.99));
    }

    @Test
    @DisplayName("Should create order with provided order number")
    @Transactional
    void createOrderWithProvidedOrderNumberShouldUseIt() {
        // Given
        Order newOrder = new Order();
        newOrder.setOrderNumber("CUSTOM-ORD-123");
        newOrder.setUser(testUser1);
        newOrder.setTotalAmount(BigDecimal.valueOf(500.00));
        newOrder.setCurrency("USD");
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setShippingAddress("789 Galaxy Road");

        // When
        Order createdOrder = orderService.createOrder(newOrder);

        // Then
        assertThat(createdOrder.getOrderNumber()).isEqualTo("CUSTOM-ORD-123");
    }

    @Test
    @DisplayName("Should update order status to SHIPPED and set shipped date")
    @Transactional
    void updateOrderStatusToShippedShouldSetShippedDate() {
        // Given
        Order pendingOrder = orderRepository.findByOrderNumber("ORD-001").orElseThrow();
        Long orderId = pendingOrder.getId();

        // When
        Order updatedOrder = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);

        // Then
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(updatedOrder.getShippedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should update order status to DELIVERED and set delivered date")
    @Transactional
    void updateOrderStatusToDeliveredShouldSetDeliveredDate() {
        // Given
        Order shippedOrder = orderRepository.findByOrderNumber("ORD-002").orElseThrow();
        Long orderId = shippedOrder.getId();

        // When
        Order updatedOrder = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);

        // Then
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(updatedOrder.getDeliveredDate()).isNotNull();
    }

    @Test
    @DisplayName("Should not override existing shipped date when updating to shipped")
    @Transactional
    void updateOrderStatusToShippedWithExistingDateShouldNotOverride() {
        // Given
        Order order = orderRepository.findByOrderNumber("ORD-002").orElseThrow();
        LocalDateTime originalShippedDate = order.getShippedDate();
        Long orderId = order.getId();

        // When
        Order updatedOrder = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);

        // Then
        assertThat(updatedOrder.getShippedDate()).isEqualTo(originalShippedDate);
    }

    @Test
    @DisplayName("Should update order successfully")
    @Transactional
    void updateOrderWithValidDataShouldUpdateOrder() {
        // Given
        Order existingOrder = orderRepository.findByOrderNumber("ORD-001").orElseThrow();
        Long orderId = existingOrder.getId();

        Order updateData = new Order();
        updateData.setStatus(OrderStatus.CONFIRMED);
        updateData.setTotalAmount(BigDecimal.valueOf(1099.99));
        updateData.setShippingAddress("Updated Address, Cosmic Lane");
        updateData.setShippedDate(LocalDateTime.now());
        updateData.setDeliveredDate(null);

        // When
        Order updatedOrder = orderService.updateOrder(orderId, updateData);

        // Then
        assertThat(updatedOrder.getId()).isEqualTo(orderId);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(updatedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1099.99));
        assertThat(updatedOrder.getShippingAddress()).isEqualTo("Updated Address, Cosmic Lane");
    }

    @Test
    @DisplayName("Should delete order successfully")
    @Transactional
    void deleteOrderWithValidIdShouldDeleteOrder() {
        // Given
        Order order = orderRepository.findByOrderNumber("ORD-003").orElseThrow();
        Long orderId = order.getId();

        // When
        orderService.deleteOrder(orderId);

        // Then
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }

    @Test
    @DisplayName("Should throw RuntimeException when deleting non-existent order")
    @Transactional
    void deleteOrderWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> orderService.deleteOrder(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found with ID: 999");
    }

    @Test
    @DisplayName("Should retrieve order items successfully")
    @Transactional
    void getOrderItemsShouldReturnOrderItems() {
        // Given
        Order order = orderRepository.findByOrderNumber("ORD-001").orElseThrow();
        createOrderItem(order, testProduct1, 2, BigDecimal.valueOf(999.99));
        createOrderItem(order, testProduct2, 1, BigDecimal.valueOf(599.99));

        // When
        List<OrderItem> orderItems = orderService.getOrderItems(order.getId());

        // Then
        assertThat(orderItems).isNotEmpty();
        assertThat(orderItems).hasSize(2);
    }

    @Test
    @DisplayName("Should calculate total revenue by user successfully")
    @Transactional
    void calculateTotalRevenueByUserShouldReturnCorrectAmount() {
        // When - testUser2 has a DELIVERED order, testUser1 does not
        BigDecimal revenue = orderService.calculateTotalRevenueByUser(testUser2.getId());

        // Then
        assertThat(revenue).isNotNull();
        assertThat(revenue).isEqualByComparingTo(BigDecimal.valueOf(1599.98));
    }

    @Test
    @DisplayName("Should return zero when user has no orders")
    @Transactional
    void calculateTotalRevenueByUserWithNoOrdersShouldReturnZero() {
        // Given
        User newUser = createUser("no.orders@galaxy.com", "No Orders User");

        // When
        BigDecimal revenue = orderService.calculateTotalRevenueByUser(newUser.getId());

        // Then
        assertThat(revenue).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should count orders by status successfully")
    @Transactional
    void countOrdersByStatusShouldReturnCorrectCount() {
        // When
        long pendingCount = orderService.countOrdersByStatus(OrderStatus.PENDING);
        long shippedCount = orderService.countOrdersByStatus(OrderStatus.SHIPPED);
        long deliveredCount = orderService.countOrdersByStatus(OrderStatus.DELIVERED);

        // Then
        assertThat(pendingCount).isEqualTo(1);
        assertThat(shippedCount).isEqualTo(1);
        assertThat(deliveredCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find old pending orders successfully")
    @Transactional
    void findOldPendingOrdersShouldReturnOldOrders() {
        // Given
        createOrder(testUser1, "ORD-OLD", OrderStatus.PENDING, BigDecimal.valueOf(100.00), LocalDateTime.now().minusDays(10));
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        // When
        List<Order> oldOrders = orderService.findOldPendingOrders(cutoffDate);

        // Then
        assertThat(oldOrders).isNotEmpty();
        assertThat(oldOrders).anyMatch(order -> order.getOrderNumber().equals("ORD-OLD"));
    }

    @Test
    @DisplayName("Should return empty list when no old pending orders exist")
    @Transactional
    void findOldPendingOrdersWithNoOldOrdersShouldReturnEmptyList() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        // When
        List<Order> oldOrders = orderService.findOldPendingOrders(cutoffDate);

        // Then
        assertThat(oldOrders).isEmpty();
    }
}
