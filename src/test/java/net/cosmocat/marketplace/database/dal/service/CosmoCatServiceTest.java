package net.cosmocat.marketplace.database.dal.service;

import net.cosmocat.marketplace.database.dto.entity.CosmoCatDTO;
import net.cosmocat.marketplace.exception.type.FeatureNotAvailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CosmoCatService Tests")
class CosmoCatServiceTest {

    @TestConfiguration
    @ComponentScan(
            basePackages = {
                    "net.cosmocat.marketplace.database.dal.service",
                    "net.cosmocat.marketplace.mapper",
                    "net.cosmocat.marketplace.aop"
            })
    @EnableAspectJAutoProxy
    static class TestConfig {
    }

    @Nested
    @DisplayName("When feature.cosmoCats is enabled")
    @SpringBootTest(classes = TestConfig.class)
    @TestPropertySource(properties = {"feature.cosmoCats.enabled=true"})
    class WhenFeatureEnabled {

        @Autowired
        private CosmoCatService cosmoCatService;

        @Test
        @DisplayName("Should retrieve all CosmoCats successfully")
        void getCosmoCatsShouldReturnAllCosmoCats() {
            // When
            List<CosmoCatDTO> cosmoCats = cosmoCatService.getCosmoCats();

            // Then
            assertThat(cosmoCats).isNotEmpty();
            assertThat(cosmoCats).hasSize(4);
            assertThat(cosmoCats)
                    .extracting(CosmoCatDTO::getName)
                    .containsExactlyInAnyOrder("Luna", "Cosmo", "Stella", "Orion");
        }

        @Test
        @DisplayName("Should return CosmoCats with correct properties")
        void getCosmoCatsShouldReturnCosmoCatsWithCorrectProperties() {
            // When
            List<CosmoCatDTO> cosmoCats = cosmoCatService.getCosmoCats();

            // Then
            CosmoCatDTO luna =
                    cosmoCats.stream().filter(cat -> cat.getName().equals("Luna")).findFirst().orElseThrow();

            assertThat(luna.getId()).isEqualTo(1L);
            assertThat(luna.getBreed()).isEqualTo("Nebula Shorthair");
            assertThat(luna.getColor()).isEqualTo("Silver");
            assertThat(luna.getAge()).isEqualTo(3);
            assertThat(luna.getImage()).isEqualTo("https://example.com/cats/luna.jpg");
            assertThat(luna.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("When feature.cosmoCats is disabled")
    @SpringBootTest(classes = TestConfig.class)
    @TestPropertySource(properties = {"feature.cosmoCats.enabled=false"})
    class WhenFeatureDisabled {

        @Autowired
        private CosmoCatService cosmoCatService;

        @Test
        @DisplayName("Should throw FeatureNotAvailableException when calling getCosmoCats")
        void getCosmoCatsShouldThrowFeatureNotAvailableException() {
            // When & Then
            assertThatThrownBy(() -> cosmoCatService.getCosmoCats())
                    .isInstanceOf(FeatureNotAvailableException.class)
                    .hasMessageContaining("cosmoCats")
                    .hasMessageContaining("not available");
        }
    }
}
