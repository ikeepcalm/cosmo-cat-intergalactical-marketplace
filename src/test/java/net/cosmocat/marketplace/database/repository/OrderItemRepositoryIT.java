package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Order;
import net.cosmocat.marketplace.database.entity.OrderItem;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.User;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.database.entity.source.OrderStatus;
import net.cosmocat.marketplace.database.projection.ProductPurchaseReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItemRepository Integration Tests")
class OrderItemRepositoryIT extends TestContainersBaseTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Category electronicsCategory;
    private Product laptop;
    private Product smartphone;
    private Order order1;
    private Order order2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private OrderItem orderItem3;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Users
        user1 = createUser("user1@example.com", "user1", "John", "Doe");
        user2 = createUser("user2@example.com", "user2", "Jane", "Smith");

        // Setup Categories
        electronicsCategory = createCategory("Electronics", "Electronic devices and gadgets", Arrays.asList("tech", "gadgets"));

        // Setup Products
        laptop = createProduct("Laptop HP Pro", "High performance laptop", 999.99, "USD", electronicsCategory, "LAPTOP001", 10);
        smartphone = createProduct("Smartphone Samsung", "Latest Android smartphone", 699.99, "USD", electronicsCategory, "PHONE001", 25);
        Product headphones = createProduct("Wireless Headphones", "Bluetooth headphones", 129.99, "USD", electronicsCategory, "HEAD001", 50);

        // Setup Orders
        order1 = createOrder(user1, "ORD001", OrderStatus.PENDING, new BigDecimal("1699.98"), "USD");
        order2 = createOrder(user2, "ORD002", OrderStatus.DELIVERED, new BigDecimal("129.99"), "USD");

        // Setup OrderItems
        orderItem1 = createOrderItem(order1, laptop, 1, new BigDecimal("999.99"), "USD");
        orderItem2 = createOrderItem(order1, smartphone, 1, new BigDecimal("699.99"), "USD");
        orderItem3 = createOrderItem(order2, headphones, 1, new BigDecimal("129.99"), "USD");
    }

    private User createUser(String email, String username, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash("dummy-hashed-password"); // Use setPasswordHash
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    private Category createCategory(String name, String description, List<String> tags) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setTags(tags);
        return categoryRepository.save(category);
    }

    private Product createProduct(String name, String description, Double price, String currency,
                                   Category category, String sku, Integer stockQuantity) {
        Product product = new Product();
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
        product.setImage("https://example.com/image.jpg");
        return productRepository.save(product);
    }

    private Order createOrder(User user, String orderNumber, OrderStatus status, BigDecimal totalAmount, String currency) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setCurrency(currency);
        return orderRepository.save(order);
    }

    private OrderItem createOrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice, String currency) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice); // Use setUnitPrice
        orderItem.setCurrency(currency); // Set currency
        orderItem.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity))); // Calculate and set subtotal
        return orderItemRepository.save(orderItem);
    }

    @Test
    @DisplayName("Should save a new order item successfully")
    void saveNewOrderItem() {
        // Given
        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setOrder(order1);
        newOrderItem.setProduct(laptop);
        newOrderItem.setQuantity(2);
        newOrderItem.setUnitPrice(new BigDecimal("999.99")); // Use setUnitPrice
        newOrderItem.setCurrency("USD"); // Set currency
        newOrderItem.setSubtotal(newOrderItem.getUnitPrice().multiply(BigDecimal.valueOf(newOrderItem.getQuantity()))); // Calculate and set subtotal

        // When
        OrderItem savedOrderItem = orderItemRepository.save(newOrderItem);

        // Then
        assertThat(savedOrderItem).isNotNull();
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(2);
        assertThat(savedOrderItem.getUnitPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(savedOrderItem.getCurrency()).isEqualTo("USD");
        assertThat(savedOrderItem.getSubtotal()).isEqualTo(new BigDecimal("1999.98"));
        assertThat(orderItemRepository.count()).isEqualTo(4); // 3 initial + 1 new
    }

    @Test
    @DisplayName("Should find order item by ID")
    void findOrderItemById() {
        // Given
        Long orderItemId = orderItem1.getId();

        // When
        Optional<OrderItem> foundOrderItem = orderItemRepository.findById(orderItemId);

        // Then
        assertThat(foundOrderItem).isPresent();
        assertThat(foundOrderItem.get().getProduct().getName()).isEqualTo("Laptop HP Pro");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent order item ID")
    void findOrderItemByIdNotFound() {
        // When
        Optional<OrderItem> foundOrderItem = orderItemRepository.findById(999L);

        // Then
        assertThat(foundOrderItem).isNotPresent();
    }

    @Test
    @DisplayName("Should find all order items")
    void findAllOrderItems() {
        // When
        List<OrderItem> orderItems = orderItemRepository.findAll();

        // Then
        assertThat(orderItems).isNotEmpty();
        assertThat(orderItems).hasSize(3);
        assertThat(orderItems).extracting(oi -> oi.getProduct().getName())
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung", "Wireless Headphones");
    }

    @Test
    @DisplayName("Should update an existing order item")
    void updateOrderItem() {
        // Given
        OrderItem existingOrderItem = orderItem1;
        existingOrderItem.setQuantity(2);
        existingOrderItem.setUnitPrice(new BigDecimal("1999.98")); // Use setUnitPrice
        existingOrderItem.setSubtotal(existingOrderItem.getUnitPrice().multiply(BigDecimal.valueOf(existingOrderItem.getQuantity()))); // Calculate and set subtotal

        // When
        OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);

        // Then
        assertThat(updatedOrderItem).isNotNull();
        assertThat(updatedOrderItem.getId()).isEqualTo(existingOrderItem.getId());
        assertThat(updatedOrderItem.getQuantity()).isEqualTo(2);
        assertThat(updatedOrderItem.getUnitPrice()).isEqualTo(new BigDecimal("1999.98"));
        assertThat(updatedOrderItem.getSubtotal()).isEqualTo(new BigDecimal("3999.96"));
    }

    @Test
    @DisplayName("Should delete an order item by ID")
    void deleteOrderItemById() {
        // Given
        Long orderItemId = orderItem3.getId();

        // When
        orderItemRepository.deleteById(orderItemId);

        // Then
        assertThat(orderItemRepository.findById(orderItemId)).isNotPresent();
        assertThat(orderItemRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find order items by order ID")
    void findByOrderId() {
        // When
        List<OrderItem> itemsForOrder1 = orderItemRepository.findByOrderId(order1.getId());
        List<OrderItem> itemsForOrder2 = orderItemRepository.findByOrderId(order2.getId());

        // Then
        assertThat(itemsForOrder1).hasSize(2);
        assertThat(itemsForOrder1).extracting(oi -> oi.getProduct().getName())
                .containsExactlyInAnyOrder("Laptop HP Pro", "Smartphone Samsung");
        assertThat(itemsForOrder2).hasSize(1);
        assertThat(itemsForOrder2).extracting(oi -> oi.getProduct().getName())
                .containsExactlyInAnyOrder("Wireless Headphones");
    }

    @Test
    @DisplayName("Should find order items by product ID")
    void findByProductId() {
        // When
        List<OrderItem> itemsForLaptop = orderItemRepository.findByProductId(laptop.getId());
        List<OrderItem> itemsForSmartphone = orderItemRepository.findByProductId(smartphone.getId());

        // Then
        assertThat(itemsForLaptop).hasSize(1);
        assertThat(itemsForLaptop.getFirst().getProduct().getName()).isEqualTo("Laptop HP Pro");
        assertThat(itemsForSmartphone).hasSize(1);
        assertThat(itemsForSmartphone.getFirst().getProduct().getName()).isEqualTo("Smartphone Samsung");
    }

    @Test
    @DisplayName("Should find order items by order ID and product ID")
    void findByOrderIdAndProductId() {
        // When
        List<OrderItem> foundItem = orderItemRepository.findByOrderIdAndProductId(order1.getId(), laptop.getId());
        List<OrderItem> notFoundItem = orderItemRepository.findByOrderIdAndProductId(order2.getId(), laptop.getId());

        // Then
        assertThat(foundItem).hasSize(1);
        assertThat(foundItem.getFirst().getProduct().getName()).isEqualTo("Laptop HP Pro");
        assertThat(notFoundItem).isEmpty();
    }

    @Test
    @DisplayName("Should count order items by product ID")
    void countByProductId() {
        // Given
        // Add another order item for laptop
        createOrderItem(order2, laptop, 1, new BigDecimal("999.99"), "USD");

        // When
        long countLaptop = orderItemRepository.countByProductId(laptop.getId());
        long countSmartphone = orderItemRepository.countByProductId(smartphone.getId());
        long countNonExistent = orderItemRepository.countByProductId(999L);

        // Then
        assertThat(countLaptop).isEqualTo(2);
        assertThat(countSmartphone).isEqualTo(1);
        assertThat(countNonExistent).isEqualTo(0);
    }

    @Test
    @DisplayName("Should get total quantity ordered by product")
    void getTotalQuantityOrderedByProduct() {
        // Given
        // Add another order item for laptop with quantity 2
        createOrderItem(order2, laptop, 2, new BigDecimal("999.99"), "USD");

        // When
        Long totalLaptopQuantity = orderItemRepository.getTotalQuantityOrderedByProduct(laptop.getId());
        Long totalSmartphoneQuantity = orderItemRepository.getTotalQuantityOrderedByProduct(smartphone.getId());
        Long totalNonExistentQuantity = orderItemRepository.getTotalQuantityOrderedByProduct(999L);

        // Then
        assertThat(totalLaptopQuantity).isEqualTo(3); // 1 (initial) + 2 (new)
        assertThat(totalSmartphoneQuantity).isEqualTo(1);
        assertThat(totalNonExistentQuantity).isNull();
    }

    @Test
    @DisplayName("Should find most ordered products")
    void findMostOrderedProducts() {
        // Given
        // Order laptop 2 more times
        createOrderItem(order1, laptop, 1, new BigDecimal("999.99"), "USD");
        createOrderItem(order2, laptop, 1, new BigDecimal("999.99"), "USD");
        // Order smartphone 1 more time
        createOrderItem(order2, smartphone, 1, new BigDecimal("699.99"), "USD");

        // When
        List<Long> mostOrderedProductIds = orderItemRepository.findMostOrderedProducts();

        // Then
        assertThat(mostOrderedProductIds).isNotEmpty();
        // Laptop (3), Smartphone (2), Headphones (1)
        assertThat(mostOrderedProductIds.get(0)).isEqualTo(laptop.getId());
        assertThat(mostOrderedProductIds.get(1)).isEqualTo(smartphone.getId());
        assertThat(mostOrderedProductIds.get(2)).isEqualTo(orderItem3.getProduct().getId());
    }

    @Test
    @DisplayName("Should find most purchased products (ProductPurchaseReport)")
    void findMostPurchasedProducts() {
        // Given
        // Order laptop 2 more times
        createOrderItem(order1, laptop, 1, new BigDecimal("999.99"), "USD");
        createOrderItem(order2, laptop, 1, new BigDecimal("999.99"), "USD");
        // Order smartphone 1 more time
        createOrderItem(order2, smartphone, 1, new BigDecimal("699.99"), "USD");

        // When
        List<ProductPurchaseReport> reports = orderItemRepository.findMostPurchasedProducts();

        // Then
        assertThat(reports).isNotEmpty();
        assertThat(reports).hasSize(3);

        // Laptop (3 purchases), Smartphone (2 purchases), Headphones (1 purchase)
        assertThat(reports.get(0).getProductName()).isEqualTo(laptop.getName());
        assertThat(reports.get(0).getPurchaseCount()).isEqualTo(3L);

        assertThat(reports.get(1).getProductName()).isEqualTo(smartphone.getName());
        assertThat(reports.get(1).getPurchaseCount()).isEqualTo(2L);

        assertThat(reports.get(2).getProductName()).isEqualTo(orderItem3.getProduct().getName());
        assertThat(reports.get(2).getPurchaseCount()).isEqualTo(1L);
    }
}
