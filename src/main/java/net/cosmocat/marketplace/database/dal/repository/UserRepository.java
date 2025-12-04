package net.cosmocat.marketplace.database.dal.repository;

import net.cosmocat.marketplace.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> searchByName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query("SELECT DISTINCT u FROM User u WHERE SIZE(u.orders) > 0")
    List<User> findUsersWithOrders();

    @Query("SELECT u FROM User u WHERE SIZE(u.orders) = 0")
    List<User> findUsersWithoutOrders();
}