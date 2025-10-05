package net.cosmocat.marketplace.mapper;

import net.cosmocat.marketplace.database.dto.entity.CategoryDTO;
import net.cosmocat.marketplace.database.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO toCategoryDTO(Category category);

    Category toCategoryEntity(CategoryDTO categoryDTO);
}