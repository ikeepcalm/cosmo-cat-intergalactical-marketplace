package net.cosmocat.marketplace.mapper;

import net.cosmocat.marketplace.database.dto.entity.CategoryDTO;
import net.cosmocat.marketplace.database.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", expression = "java(category.getProducts() != null ? category.getProducts().size() : 0)")
    CategoryDTO toDTO(Category category);

    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);
}