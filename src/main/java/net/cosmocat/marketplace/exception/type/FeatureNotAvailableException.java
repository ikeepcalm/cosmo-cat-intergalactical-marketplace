package net.cosmocat.marketplace.exception.type;

public class FeatureNotAvailableException extends RuntimeException {

    public FeatureNotAvailableException(String featureName) {
        super(String.format("Feature '%s' is not available", featureName));
    }

    public FeatureNotAvailableException(String featureName, String additionalInfo) {
        super(String.format("Feature '%s' is not available: %s", featureName, additionalInfo));
    }
}