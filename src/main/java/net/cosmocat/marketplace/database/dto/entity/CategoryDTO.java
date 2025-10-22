package net.cosmocat.marketplace.database.dto.entity;

import java.util.List;
import lombok.Value;

@Value
public class CategoryDTO {
  Long id;
  String name;
  String description;
  List<String> tags;
  Integer productCount;
}
