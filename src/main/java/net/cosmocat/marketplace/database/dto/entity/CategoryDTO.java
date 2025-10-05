package net.cosmocat.marketplace.database.dto.entity;

import lombok.Value;

import java.util.List;

@Value
public class CategoryDTO {
    Long id;
    String name;
    String description;
    List<String> tags;
    Integer productCount;
}