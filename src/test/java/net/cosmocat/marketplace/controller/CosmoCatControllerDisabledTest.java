package net.cosmocat.marketplace.controller;

import net.cosmocat.marketplace.config.PostgreSQLTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"feature.cosmoCats.enabled=false"})
@DisplayName("CosmoCatController Integration Tests - Feature Disabled")
class CosmoCatControllerDisabledTest {

    private static final PostgreSQLContainer<?> postgresContainer = PostgreSQLTestContainer.getInstance();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("Should return 403 Forbidden when feature toggle is disabled")
    void shouldReturnForbiddenWhenFeatureDisabled() throws Exception {
        // When & Then
        mockMvc
                .perform(get("/api/cosmo-cats"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title", is("Feature Not Available")))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("cosmoCats")));
    }
}
