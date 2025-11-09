package net.cosmocat.marketplace.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"feature.cosmoCats.enabled=false"})
@DisplayName("CosmoCatController Integration Tests - Feature Disabled")
class CosmoCatControllerDisabledTest {

  @Autowired private MockMvc mockMvc;

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
