package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.dal.repository.OrderRepository;
import net.cosmocat.marketplace.database.dal.repository.UserRepository;
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

@DisplayName("UserRepository Integration Tests")
class UserRepositoryIT extends TestContainersBaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User user1;
    private User user2;
    private User user3; // User without orders

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Users
        user1 = createUser("user1@example.com", "user1", "John", "Doe");
        user2 = createUser("user2@example.com", "user2", "Jane", "Smith");
        user3 = createUser("user3@example.com", "user3", "Peter", "Jones");

        // Setup Orders for user1 and user2
        createOrder(user1, "ORD001", OrderStatus.PENDING, new BigDecimal("100.00"), LocalDateTime.now().minusDays(5), "USD");
        createOrder(user1, "ORD002", OrderStatus.DELIVERED, new BigDecimal("250.50"), LocalDateTime.now().minusDays(2), "USD");
        createOrder(user2, "ORD003", OrderStatus.SHIPPED, new BigDecimal("50.00"), LocalDateTime.now().minusDays(1), "USD");
    }

    private User createUser(String email, String username, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash("hashedPassword123"); // Corrected: set passwordHash
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    private Order createOrder(User user, String orderNumber, OrderStatus status, BigDecimal totalAmount, LocalDateTime orderDate, String currency) {
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
    @DisplayName("Should save a new user successfully")
    void saveNewUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setUsername("newuser");
        newUser.setPasswordHash("newHashedPassword");
        newUser.setFirstName("Alice");
        newUser.setLastName("Brown");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(userRepository.count()).isEqualTo(4); // 3 initial + 1 new
    }

    @Test
    @DisplayName("Should find user by ID")
    void findUserById() {
        // Given
        Long userId = user1.getId();

        // When
        Optional<User> foundUser = userRepository.findById(userId);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent user ID")
    void findUserByIdNotFound() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("Should find all users")
    void findAllUsers() {
        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    @DisplayName("Should update an existing user")
    void updateUser() {
        // Given
        User existingUser = user1;
        existingUser.setFirstName("Jonathan");
        existingUser.setLastName("Smith");

        // When
        User updatedUser = userRepository.save(existingUser);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(existingUser.getId());
        assertThat(updatedUser.getFirstName()).isEqualTo("Jonathan");
        assertThat(updatedUser.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("Should delete a user by ID")
    void deleteUserById() {
        // Given
        Long userId = user3.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        assertThat(userRepository.findById(userId)).isNotPresent();
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("user2@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("user2");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent email")
    void findByEmailNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("user1");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent username")
    void findByUsernameNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void existsByEmail() {
        // When
        boolean exists = userRepository.existsByEmail("user3@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void existsByUsername() {
        // When
        boolean exists = userRepository.existsByUsername("user2");
        boolean notExists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should search users by name (first or last name)")
    void searchByName() {
        // When
        List<User> foundUsers = userRepository.searchByName("john", "doe"); // Case-insensitive
        List<User> foundUsersPartial = userRepository.searchByName("jane", "smith");
        List<User> foundUsersFirstNameOnly = userRepository.searchByName("peter", "");

        // Then
        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.getFirst().getUsername()).isEqualTo("user1");
        assertThat(foundUsersPartial).hasSize(1);
        assertThat(foundUsersPartial.getFirst().getUsername()).isEqualTo("user2");
        assertThat(foundUsersFirstNameOnly).hasSize(1);
        assertThat(foundUsersFirstNameOnly.getFirst().getUsername()).isEqualTo("user3");
    }

    @Test
    @DisplayName("Should find users with orders")
    void findUsersWithOrders() {
        // When
        List<User> usersWithOrders = userRepository.findUsersWithOrders();

        // Then
        assertThat(usersWithOrders).hasSize(2);
        assertThat(usersWithOrders).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("Should find users without orders")
    void findUsersWithoutOrders() {
        // When
        List<User> usersWithoutOrders = userRepository.findUsersWithoutOrders();

        // Then
        assertThat(usersWithoutOrders).hasSize(1);
        assertThat(usersWithoutOrders.getFirst().getUsername()).isEqualTo("user3");
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving user with duplicate email")
    void saveUserWithDuplicateEmailShouldThrowException() {
        // Given
        User duplicateUser = new User();
        duplicateUser.setEmail("user1@example.com"); // Duplicate email
        duplicateUser.setUsername("anotheruser");
        duplicateUser.setPasswordHash("hashedPassword");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("Email");

        // When & Then
        assertThatThrownBy(() -> userRepository.save(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving user with duplicate username")
    void saveUserWithDuplicateUsernameShouldThrowException() {
        // Given
        User duplicateUser = new User();
        duplicateUser.setEmail("anotheremail@example.com");
        duplicateUser.setUsername("user2"); // Duplicate username
        duplicateUser.setPasswordHash("hashedPassword");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("Username");

        // When & Then
        assertThatThrownBy(() -> userRepository.save(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
