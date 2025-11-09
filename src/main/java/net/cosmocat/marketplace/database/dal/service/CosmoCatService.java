package net.cosmocat.marketplace.database.dal.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.aop.FeatureToggle;
import net.cosmocat.marketplace.database.dto.entity.CosmoCatDTO;
import net.cosmocat.marketplace.database.entity.CosmoCat;
import net.cosmocat.marketplace.mapper.CosmoCatMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CosmoCatService {

  private final CosmoCatMapper cosmoCatMapper;
  private final Map<Long, CosmoCat> cosmoCats = new HashMap<>();
  private final AtomicLong catIdGenerator = new AtomicLong(1);

  public CosmoCatService(CosmoCatMapper cosmoCatMapper) {
    this.cosmoCatMapper = cosmoCatMapper;
    initMockData();
  }

  private void initMockData() {
    createMockCosmoCat("Luna", "Nebula Shorthair", "Silver", 3, "https://example.com/cats/luna.jpg");
    createMockCosmoCat("Cosmo", "Galactic Persian", "White", 5, "https://example.com/cats/cosmo.jpg");
    createMockCosmoCat("Stella", "Stardust Siamese", "Blue", 2, "https://example.com/cats/stella.jpg");
    createMockCosmoCat("Orion", "Constellation Maine Coon", "Orange", 4, "https://example.com/cats/orion.jpg");
  }

  private void createMockCosmoCat(String name, String breed, String color, Integer age, String image) {
    CosmoCat cat = new CosmoCat();
    Long id = catIdGenerator.getAndIncrement();
    cat.setId(id);
    cat.setName(name);
    cat.setBreed(breed);
    cat.setColor(color);
    cat.setAge(age);
    cat.setImage(image);
    cat.setCreatedAt(LocalDateTime.now());

    cosmoCats.put(id, cat);
  }

  @FeatureToggle("cosmoCats")
  public List<CosmoCatDTO> getCosmoCats() {
    log.info("Retrieving all CosmoCats");
    List<CosmoCat> catList = new ArrayList<>(cosmoCats.values());
    return cosmoCatMapper.toCosmoCatDTOList(catList);
  }

}
