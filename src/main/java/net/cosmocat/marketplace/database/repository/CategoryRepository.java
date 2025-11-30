package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.database.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT c FROM categories c JOIN c.tags t WHERE t = :tag")
    List<Category> findByTag(@Param("tag") String tag);

    @Query("SELECT DISTINCT c FROM categories c WHERE SIZE(c.products) > 0")
    List<Category> findCategoriesWithProducts();

    @Query("SELECT c FROM categories c WHERE SIZE(c.products) = 0")
    List<Category> findEmptyCategories();
}