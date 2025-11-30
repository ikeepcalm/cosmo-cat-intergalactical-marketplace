package net.cosmocat.marketplace.aop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@ConfigurationProperties(prefix = "feature")
public class FeatureToggleService {

    private final Map<String, FeatureConfig> features = new HashMap<>();

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

    @Setter
    @Getter
    public static class FeatureConfig {
        private boolean enabled;
    }
}