package net.cosmocat.marketplace.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "feature")
@Getter
public class FeatureToggleService {

  private Map<String, FeatureConfig> features = new HashMap<>();

  public boolean isFeatureEnabled(String featureName) {
    FeatureConfig config = features.get(featureName);
    return config != null && config.isEnabled();
  }

  public void setCosmoCats(FeatureConfig cosmoCats) {
    features.put("cosmoCats", cosmoCats);
  }

  public void setKittyProducts(FeatureConfig kittyProducts) {
    features.put("kittyProducts", kittyProducts);
  }

  @Getter
  public static class FeatureConfig {
    private boolean enabled;

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}