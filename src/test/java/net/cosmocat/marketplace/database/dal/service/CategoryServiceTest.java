package net.cosmocat.marketplace.database.dal.service;

import net.cosmocat.marketplace.TestContainersBaseTest;
import net.cosmocat.marketplace.database.dto.entity.CategoryDTO;
import net.cosmocat.marketplace.database.dto.request.CategoryCreateDTO;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.database.dal.repository.CategoryRepository;
import net.cosmocat.marketplace.database.dal.repository.ProductRepository;
import net.cosmocat.marketplace.exception.type.CategoryConflictException;
import net.cosmocat.marketplace.exception.type.CategoryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CategoryService Tests")
class CategoryServiceTest extends TestContainersBaseTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category stellarElectronics;
    private Category cosmicBooks;
    private Category galacticClothing;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        stellarElectronics = createCategory("Stellar Electronics", "Electronic devices from across the galaxy");
        cosmicBooks = createCategory("Cosmic Books", "Literature from various star systems");
        galacticClothing = createCategory("Galactic Clothing", "Fashion items for space travelers");
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setTags(new ArrayList<>(Arrays.asList("cosmic", "featured")));
        return categoryRepository.save(category);
    }

    private void createProduct(String name, String sku, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(99.99);
        product.setCurrency("USD");
        product.setCategory(category);
        product.setSku(sku);
        product.setStockQuantity(10);
        product.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        product.setWeight(1.0);
        product.setDimensions("10x10x10");
        product.setImage("https://example.com/image.jpg");
        productRepository.save(product);
    }

    @Test
    @DisplayName("Should retrieve all categories successfully")
    @Transactional
    void getAllCategoriesShouldReturnAllCategories() {
        // When
        List<CategoryDTO> categories = categoryService.getAllCategories();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories).hasSize(3);
        assertThat(categories)
                .extracting(CategoryDTO::name)
                .containsExactlyInAnyOrder("Stellar Electronics", "Cosmic Books", "Galactic Clothing");
    }

    @Test
    @DisplayName("Should retrieve category by ID successfully")
    @Transactional
    void getCategoryByIdWithValidIdShouldReturnCategory() {
        // Given
        Long categoryId = stellarElectronics.getId();

        // When
        CategoryDTO category = categoryService.getCategoryById(categoryId);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.id()).isEqualTo(categoryId);
        assertThat(category.name()).isEqualTo("Stellar Electronics");
        assertThat(category.description()).isEqualTo("Electronic devices from across the galaxy");
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category ID doesn't exist")
    @Transactional
    void getCategoryByIdWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(nonExistentId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create new category successfully")
    @Transactional
    void createCategoryWithValidDataShouldCreateCategory() {
        // Given
        CategoryCreateDTO createRequest = new CategoryCreateDTO(
                "Stellar Toys",
                "Toys from various galaxies",
                Arrays.asList("toys", "cosmic", "fun")
        );

        // When
        CategoryDTO createdCategory = categoryService.createCategory(createRequest);

        // Then
        assertThat(createdCategory).isNotNull();
        assertThat(createdCategory.id()).isNotNull();
        assertThat(createdCategory.name()).isEqualTo("Stellar Toys");
        assertThat(createdCategory.description()).isEqualTo("Toys from various galaxies");
        assertThat(createdCategory.tags()).containsExactlyInAnyOrder("toys", "cosmic", "fun");
    }

    @Test
    @DisplayName("Should throw CategoryConflictException when category name already exists")
    @Transactional
    void createCategoryWithDuplicateNameShouldThrowException() {
        // Given
        CategoryCreateDTO createRequest = new CategoryCreateDTO(
                "Stellar Electronics",
                "Another electronics category",
                Arrays.asList("electronics", "cosmic")
        );

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                .isInstanceOf(CategoryConflictException.class)
                .hasMessageContaining("Stellar Electronics");
    }

    @Test
    @DisplayName("Should update existing category successfully")
    @Transactional
    void updateCategoryWithValidDataShouldUpdateCategory() {
        // Given
        Long categoryId = cosmicBooks.getId();
        CategoryCreateDTO updateRequest = new CategoryCreateDTO(
                "Cosmic Books & Literature",
                "Updated description for books category",
                Arrays.asList("books", "cosmic", "reading")
        );

        // When
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, updateRequest);

        // Then
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.id()).isEqualTo(categoryId);
        assertThat(updatedCategory.name()).isEqualTo("Cosmic Books & Literature");
        assertThat(updatedCategory.description()).isEqualTo("Updated description for books category");
        assertThat(updatedCategory.tags()).containsExactlyInAnyOrder("books", "cosmic", "reading");
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when updating non-existent category")
    @Transactional
    void updateCategoryWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        CategoryCreateDTO updateRequest = new CategoryCreateDTO(
                "Updated Name",
                "Updated description",
                Arrays.asList("tag1", "tag2")
        );

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(nonExistentId, updateRequest))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should throw CategoryConflictException when updating to duplicate name")
    @Transactional
    void updateCategoryWithDuplicateNameShouldThrowException() {
        // Given
        Long categoryId = cosmicBooks.getId();
        CategoryCreateDTO updateRequest = new CategoryCreateDTO(
                "Stellar Electronics",
                "Trying to use existing name",
                List.of("electronics")
        );

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateRequest))
                .isInstanceOf(CategoryConflictException.class)
                .hasMessageContaining("Stellar Electronics");
    }

    @Test
    @DisplayName("Should allow updating category with same name")
    @Transactional
    void updateCategoryWithSameNameShouldSucceed() {
        // Given
        Long categoryId = cosmicBooks.getId();
        CategoryCreateDTO updateRequest = new CategoryCreateDTO(
                "Cosmic Books",
                "Updated description but same name",
                Arrays.asList("books", "updated")
        );

        // When
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, updateRequest);

        // Then
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.name()).isEqualTo("Cosmic Books");
        assertThat(updatedCategory.description()).isEqualTo("Updated description but same name");
    }

    @Test
    @DisplayName("Should delete category successfully")
    @Transactional
    void deleteCategoryWithValidIdShouldDeleteCategory() {
        // Given
        Long categoryId = galacticClothing.getId();

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when deleting non-existent category")
    @Transactional
    void deleteCategoryWithInvalidIdShouldThrowException() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(nonExistentId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should search categories by name successfully")
    @Transactional
    void searchCategoriesByNameWithValidNameShouldReturnMatchingCategories() {
        // When
        List<CategoryDTO> results = categoryService.searchCategoriesByName("Electronics");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).containsIgnoringCase("Electronics");
    }

    @Test
    @DisplayName("Should search categories case-insensitively")
    @Transactional
    void searchCategoriesByNameCaseInsensitiveShouldReturnMatchingCategories() {
        // When
        List<CategoryDTO> results = categoryService.searchCategoriesByName("cosmic");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().name()).containsIgnoringCase("Cosmic");
    }

    @Test
    @DisplayName("Should return empty list when no categories match search")
    @Transactional
    void searchCategoriesByNameWithNonMatchingNameShouldReturnEmptyList() {
        // When
        List<CategoryDTO> results = categoryService.searchCategoriesByName("NonExistentCategory");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should find categories by tag successfully")
    @Transactional
    void findCategoriesByTagWithValidTagShouldReturnMatchingCategories() {
        // When
        List<CategoryDTO> results = categoryService.findCategoriesByTag("cosmic");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should return empty list when no categories have the tag")
    @Transactional
    void findCategoriesByTagWithNonExistentTagShouldReturnEmptyList() {
        // When
        List<CategoryDTO> results = categoryService.findCategoriesByTag("nonexistent");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should find categories with products")
    @Transactional
    void findCategoriesWithProductsShouldReturnOnlyCategoriesWithProducts() {
        // Given
        createProduct("Stellar Laptop", "LAPTOP-COSMIC", stellarElectronics);
        createProduct("Cosmic Novel", "BOOK-COSMIC", cosmicBooks);
        // galacticClothing has no products

        // When
        List<CategoryDTO> results = categoryService.findCategoriesWithProducts();

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(CategoryDTO::name)
                .containsExactlyInAnyOrder("Stellar Electronics", "Cosmic Books");
    }

    @Test
    @DisplayName("Should find empty categories")
    @Transactional
    void findEmptyCategoriesShouldReturnCategoriesWithoutProducts() {
        // Given
        createProduct("Stellar Laptop", "LAPTOP-COSMIC2", stellarElectronics);
        // cosmicBooks and galacticClothing have no products

        // When
        List<CategoryDTO> results = categoryService.findEmptyCategories();

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(CategoryDTO::name)
                .containsExactlyInAnyOrder("Cosmic Books", "Galactic Clothing");
    }

    @Test
    @DisplayName("Should return all categories as empty when no products exist")
    @Transactional
    void findEmptyCategoriesWithNoProductsShouldReturnAllCategories() {
        // When
        List<CategoryDTO> results = categoryService.findEmptyCategories();

        // Then
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Should return empty list when all categories have products")
    @Transactional
    void findEmptyCategoriesWhenAllHaveProductsShouldReturnEmptyList() {
        // Given
        createProduct("Product 1", "SKU1", stellarElectronics);
        createProduct("Product 2", "SKU2", cosmicBooks);
        createProduct("Product 3", "SKU3", galacticClothing);

        // When
        List<CategoryDTO> results = categoryService.findEmptyCategories();

        // Then
        assertThat(results).isEmpty();
    }
}
