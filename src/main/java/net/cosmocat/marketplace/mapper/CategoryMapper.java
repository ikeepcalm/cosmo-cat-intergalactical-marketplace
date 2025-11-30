package net.cosmocat.marketplace.mapper;

import net.cosmocat.marketplace.database.dto.entity.CategoryDTO;
import net.cosmocat.marketplace.database.dto.request.CategoryCreateDTO;
import net.cosmocat.marketplace.database.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", ignore = true)
    CategoryDTO toCategoryDTO(Category category);

    List<CategoryDTO> toCategoryDTOList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toCategoryEntity(CategoryCreateDTO categoryCreateDTO);

    @Mapping(target = "products", ignore = true)
    Category toCategoryEntity(CategoryDTO categoryDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateCategoryEntityFromRequest(CategoryCreateDTO request, @MappingTarget Category category);
}
