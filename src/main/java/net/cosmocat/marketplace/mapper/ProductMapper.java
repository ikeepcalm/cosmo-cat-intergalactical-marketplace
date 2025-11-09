package net.cosmocat.marketplace.mapper;

import java.util.List;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateDTO;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateDTO;
import net.cosmocat.marketplace.database.entity.Product;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    uses = {CategoryMapper.class})
public interface ProductMapper {

  ProductDTO toProductDTO(Product product);

  List<ProductDTO> toProductDTOList(List<Product> products);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "category", ignore = true)
  Product toProductEntity(ProductCreateDTO request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "category", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateProductEntityFromRequest(ProductUpdateDTO request, @MappingTarget Product product);
}
