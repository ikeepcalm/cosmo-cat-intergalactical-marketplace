package net.cosmocat.marketplace.database.dto.entity;

import java.time.LocalDateTime;
import lombok.Value;

@Value
public class CosmoCatDTO {
  Long id;
  String name;
  String breed;
  String color;
  Integer age;
  String image;
  LocalDateTime createdAt;
}
