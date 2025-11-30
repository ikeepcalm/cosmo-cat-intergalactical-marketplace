package net.cosmocat.marketplace.database.repository;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CategoryRepository Integration Tests")
class CategoryRepositoryIT extends TestContainersBaseTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository; // Needed to test categories with products

    private Category electronicsCategory;
    private Category booksCategory;
    private Category clothingCategory;
    private Category emptyCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronicsCategory = createCategory("Electronics", "Electronic devices and gadgets", Arrays.asList("tech", "gadgets"));
        booksCategory = createCategory("Books", "Books and literature", Arrays.asList("reading", "education"));
        clothingCategory = createCategory("Clothing", "Apparel and fashion items", Arrays.asList("fashion", "wearables"));
        emptyCategory = createCategory("Empty", "Category with no products", Arrays.asList("misc"));

        createProduct("Laptop HP Pro", "High performance laptop", 999.99, "USD", electronicsCategory, "LAPTOP001", 10);
        createProduct("Smartphone Samsung", "Latest Android smartphone", 699.99, "USD", electronicsCategory, "PHONE001", 25);
        createProduct("Java Programming Book", "Complete guide to Java programming", 49.99, "USD", booksCategory, "BOOK001", 50);
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

    @Test
    @DisplayName("Should save a new category successfully")
    void saveNewCategory() {
        // Given
        Category newCategory = new Category();
        newCategory.setName("Home Appliances");
        newCategory.setDescription("Appliances for home use");
        newCategory.setTags(Arrays.asList("home", "kitchen"));

        // When
        Category savedCategory = categoryRepository.save(newCategory);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Home Appliances");
        assertThat(categoryRepository.count()).isEqualTo(5); // 4 initial + 1 new
    }

    @Test
    @DisplayName("Should find category by ID")
    void findCategoryById() {
        // Given
        Long categoryId = electronicsCategory.getId();

        // When
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);

        // Then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent category ID")
    void findCategoryByIdNotFound() {
        // When
        Optional<Category> foundCategory = categoryRepository.findById(999L);

        // Then
        assertThat(foundCategory).isNotPresent();
    }

    @Test
    @DisplayName("Should find all categories")
    void findAllCategories() {
        // When
        List<Category> categories = categoryRepository.findAll();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories).hasSize(4);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Electronics", "Books", "Clothing", "Empty");
    }

    @Test
    @DisplayName("Should update an existing category")
    void updateCategory() {
        // Given
        Category existingCategory = categoryRepository.findByName("Clothing").orElseThrow();
        existingCategory.setDescription("Updated apparel and fashion items");
        existingCategory.setTags(Arrays.asList("fashion", "new-arrivals"));

        // When
        Category updatedCategory = categoryRepository.save(existingCategory);

        // Then
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.getId()).isEqualTo(existingCategory.getId());
        assertThat(updatedCategory.getDescription()).isEqualTo("Updated apparel and fashion items");
        assertThat(updatedCategory.getTags()).contains("new-arrivals");
    }

    @Test
    @DisplayName("Should delete a category by ID")
    void deleteCategoryById() {
        // Given
        Long categoryId = emptyCategory.getId();

        // When
        categoryRepository.deleteById(categoryId);

        // Then
        assertThat(categoryRepository.findById(categoryId)).isNotPresent();
        assertThat(categoryRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find category by name")
    void findByName() {
        // When
        Optional<Category> foundCategory = categoryRepository.findByName("Books");

        // Then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getDescription()).isEqualTo("Books and literature");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent category name")
    void findByNameNotFound() {
        // When
        Optional<Category> foundCategory = categoryRepository.findByName("NonExistent");

        // Then
        assertThat(foundCategory).isNotPresent();
    }

    @Test
    @DisplayName("Should check if category exists by name")
    void existsByName() {
        // When
        boolean exists = categoryRepository.existsByName("Electronics");
        boolean notExists = categoryRepository.existsByName("NonExistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find categories by name containing ignore case")
    void findByNameContainingIgnoreCase() {
        // When
        List<Category> foundCategories = categoryRepository.findByNameContainingIgnoreCase("book");

        // Then
        assertThat(foundCategories).hasSize(1);
        assertThat(foundCategories.getFirst().getName()).isEqualTo("Books");
    }

    @Test
    @DisplayName("Should find categories by tag")
    void findByTag() {
        // When
        List<Category> techCategories = categoryRepository.findByTag("tech");
        List<Category> fashionCategories = categoryRepository.findByTag("fashion");
        List<Category> nonExistentTagCategories = categoryRepository.findByTag("unknown");

        // Then
        assertThat(techCategories).hasSize(1);
        assertThat(techCategories.getFirst().getName()).isEqualTo("Electronics");
        assertThat(fashionCategories).hasSize(1);
        assertThat(fashionCategories.getFirst().getName()).isEqualTo("Clothing");
        assertThat(nonExistentTagCategories).isEmpty();
    }

    @Test
    @DisplayName("Should find categories with products")
    void findCategoriesWithProducts() {
        // When
        List<Category> categoriesWithProducts = categoryRepository.findCategoriesWithProducts();

        // Then
        assertThat(categoriesWithProducts).hasSize(2); // Electronics, Books
        assertThat(categoriesWithProducts).extracting(Category::getName)
                .containsExactlyInAnyOrder("Electronics", "Books");
    }

    @Test
    @DisplayName("Should find empty categories")
    void findEmptyCategories() {
        // When
        List<Category> emptyCategories = categoryRepository.findEmptyCategories();

        // Then
        assertThat(emptyCategories).hasSize(2); // Clothing, Empty
        assertThat(emptyCategories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Clothing", "Empty");
    }

    @Test
    @DisplayName("Should throw DataIntegrityViolationException when saving category with duplicate name")
    void saveCategoryWithDuplicateNameShouldThrowException() {
        // Given
        Category duplicateCategory = new Category();
        duplicateCategory.setName("Electronics"); // Duplicate name
        duplicateCategory.setDescription("Another electronics category");
        duplicateCategory.setTags(Arrays.asList("duplicate"));

        // When & Then
        assertThatThrownBy(() -> categoryRepository.save(duplicateCategory))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
