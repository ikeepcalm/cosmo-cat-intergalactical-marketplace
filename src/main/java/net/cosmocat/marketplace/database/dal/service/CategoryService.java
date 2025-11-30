package net.cosmocat.marketplace.database.dal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.database.dto.entity.CategoryDTO;
import net.cosmocat.marketplace.database.dto.request.CategoryCreateDTO;
import net.cosmocat.marketplace.database.entity.Category;
import net.cosmocat.marketplace.database.repository.CategoryRepository;
import net.cosmocat.marketplace.exception.type.CategoryConflictException;
import net.cosmocat.marketplace.exception.type.CategoryNotFoundException;
import net.cosmocat.marketplace.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDTO> getAllCategories() {
        log.info("Retrieving all categories");
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toCategoryDTOList(categories);
    }

    public CategoryDTO getCategoryById(Long id) {
        log.info("Retrieving category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> CategoryNotFoundException.forId(id));
        return categoryMapper.toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryCreateDTO request) {
        log.info("Creating new category: {}", request.name());

        if (categoryRepository.existsByName(request.name())) {
            throw CategoryConflictException.forDuplicateName(request.name());
        }

        Category category = categoryMapper.toCategoryEntity(request);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        return categoryMapper.toCategoryDTO(savedCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryCreateDTO request) {
        log.info("Updating category with ID: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> CategoryNotFoundException.forId(id));

        if (!existingCategory.getName().equals(request.name())
            && categoryRepository.existsByName(request.name())) {
            throw CategoryConflictException.forDuplicateName(request.name());
        }

        categoryMapper.updateCategoryEntityFromRequest(request, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Category updated successfully with ID: {}", id);

        return categoryMapper.toCategoryDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw CategoryNotFoundException.forId(id);
        }
        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with ID: {}", id);
    }

    public List<CategoryDTO> searchCategoriesByName(String name) {
        log.info("Searching categories by name: {}", name);
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categoryMapper.toCategoryDTOList(categories);
    }

    public List<CategoryDTO> findCategoriesByTag(String tag) {
        log.info("Finding categories by tag: {}", tag);
        List<Category> categories = categoryRepository.findByTag(tag);
        return categoryMapper.toCategoryDTOList(categories);
    }

    public List<CategoryDTO> findCategoriesWithProducts() {
        log.info("Finding categories with products");
        List<Category> categories = categoryRepository.findCategoriesWithProducts();
        return categoryMapper.toCategoryDTOList(categories);
    }

    public List<CategoryDTO> findEmptyCategories() {
        log.info("Finding empty categories");
        List<Category> categories = categoryRepository.findEmptyCategories();
        return categoryMapper.toCategoryDTOList(categories);
    }
}