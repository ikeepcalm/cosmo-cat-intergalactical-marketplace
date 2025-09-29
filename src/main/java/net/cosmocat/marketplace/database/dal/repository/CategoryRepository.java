package net.cosmocat.marketplace.database.dal.repository;

import net.cosmocat.marketplace.database.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CategoryRepository {

    private final Map<Long, Category> categories = new HashMap<>();

    public CategoryRepository() {
        initMockData();
    }

    private void initMockData() {
        Category electronics = new Category();
        electronics.setId(1L);
        electronics.setName("Electronics");
        electronics.setDescription("Electronic devices and gadgets");
        electronics.setTags(Arrays.asList("technology", "popular"));
        categories.put(1L, electronics);

        Category books = new Category();
        books.setId(2L);
        books.setName("Books");
        books.setDescription("Books and literature");
        books.setTags(Arrays.asList("education", "reading"));
        categories.put(2L, books);

        Category clothing = new Category();
        clothing.setId(3L);
        clothing.setName("Clothing");
        clothing.setDescription("Apparel and fashion items");
        clothing.setTags(Arrays.asList("fashion", "apparel"));
        categories.put(3L, clothing);
    }

    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(categories.get(id));
    }

    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }
}