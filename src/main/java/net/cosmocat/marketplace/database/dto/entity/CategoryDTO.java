package net.cosmocat.marketplace.database.dto.entity;

import java.util.List;

public record CategoryDTO(Long id, String name, String description, List<String> tags, Integer productCount) {
}
