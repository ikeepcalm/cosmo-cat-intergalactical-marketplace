package net.cosmocat.marketplace.database.dto.entity;

import java.time.LocalDateTime;

public record CosmoCatDTO(Long id, String name, String breed, String color, Integer age, String image,
                          LocalDateTime createdAt) {
}
