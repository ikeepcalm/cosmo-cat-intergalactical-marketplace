package net.cosmocat.marketplace.mapper;

import net.cosmocat.marketplace.database.dto.entity.CosmoCatDTO;
import net.cosmocat.marketplace.database.entity.CosmoCat;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CosmoCatMapper {

    CosmoCatDTO toCosmoCatDTO(CosmoCat cosmoCat);

    List<CosmoCatDTO> toCosmoCatDTOList(List<CosmoCat> cosmoCats);
}
