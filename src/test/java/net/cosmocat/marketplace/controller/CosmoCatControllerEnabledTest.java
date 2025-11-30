package net.cosmocat.marketplace.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"feature.cosmoCats.enabled=true"})
@DisplayName("CosmoCatController Integration Tests - Feature Enabled")
class CosmoCatControllerEnabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("Should return all CosmoCats successfully when feature is enabled")
    void shouldReturnCosmoCatsWhenFeatureEnabled() throws Exception {
        // When & Then
        mockMvc
                .perform(get("/api/cosmo-cats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name", is("Luna")))
                .andExpect(jsonPath("$[0].breed", is("Nebula Shorthair")))
                .andExpect(jsonPath("$[0].color", is("Silver")))
                .andExpect(jsonPath("$[0].age", is(3)))
                .andExpect(jsonPath("$[1].name", is("Cosmo")))
                .andExpect(jsonPath("$[1].breed", is("Galactic Persian")))
                .andExpect(jsonPath("$[2].name", is("Stella")))
                .andExpect(jsonPath("$[3].name", is("Orion")));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return CosmoCats with all required fields when feature is enabled")
    void shouldReturnCosmoCatsWithAllFields() throws Exception {
        // When & Then
        mockMvc
                .perform(get("/api/cosmo-cats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].breed").exists())
                .andExpect(jsonPath("$[0].color").exists())
                .andExpect(jsonPath("$[0].age").exists())
                .andExpect(jsonPath("$[0].image").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }
}
