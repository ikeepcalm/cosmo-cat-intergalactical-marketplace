package net.cosmocat.marketplace.mapper;

import net.cosmocat.marketplace.database.dto.entity.CategoryDTO;
import net.cosmocat.marketplace.database.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(target = "productCount", ignore = true)
  CategoryDTO toCategoryDTO(Category category);

  @Mapping(target = "products", ignore = true)
  Category toCategoryEntity(CategoryDTO categoryDTO);
}
