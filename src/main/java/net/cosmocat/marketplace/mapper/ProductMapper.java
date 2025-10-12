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

  ProductDTO toDTO(Product product);

  List<ProductDTO> toDTOList(List<Product> products);

  Product toEntity(ProductCreateDTO request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromRequest(ProductUpdateDTO request, @MappingTarget Product product);
}
