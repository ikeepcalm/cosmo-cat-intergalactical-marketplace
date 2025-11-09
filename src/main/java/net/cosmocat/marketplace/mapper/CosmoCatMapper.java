package net.cosmocat.marketplace.mapper;

import java.util.List;
import net.cosmocat.marketplace.database.dto.entity.CosmoCatDTO;
import net.cosmocat.marketplace.database.entity.CosmoCat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CosmoCatMapper {

  CosmoCatDTO toCosmoCatDTO(CosmoCat cosmoCat);

  List<CosmoCatDTO> toCosmoCatDTOList(List<CosmoCat> cosmoCats);
}
