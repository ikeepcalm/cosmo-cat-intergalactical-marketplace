package net.cosmocat.marketplace.config;

import net.cosmocat.marketplace.database.dal.service.ProductService;
import net.cosmocat.marketplace.mapper.ProductMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MappersTestConfig {

    @Bean
    public ProductMapper productMapper() {
        return Mappers.getMapper(ProductMapper.class);
    }

    @Bean
    public ProductService productService() {
        return Mappers.getMapper(ProductService.class);
    }
}