package net.cosmocat.marketplace.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.exception.type.FeatureNotAvailableException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {

  private final FeatureToggleService featureToggleService;

  @Around("@annotation(net.cosmocat.marketplace.config.FeatureToggle)")
  public Object checkFeatureToggle(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    FeatureToggle featureToggle = signature.getMethod().getAnnotation(FeatureToggle.class);

    String featureName = featureToggle.value();

    log.debug("Checking feature toggle for: {}", featureName);

    if (featureToggleService.isFeatureEnabled(featureName)) {
      log.debug("Feature '{}' is enabled, proceeding with execution", featureName);
      return joinPoint.proceed();
    } else {
      log.warn(
          "Feature '{}' is disabled, throwing FeatureNotAvailableException for method: {}",
          featureName,
          signature.getMethod().getName());
      throw new FeatureNotAvailableException(featureName);
    }
  }
}