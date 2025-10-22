package net.cosmocat.marketplace.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CosmicWordCheckValidator implements ConstraintValidator<CosmicWordCheck, String> {

  private static final List<String> COSMIC_TERMS =
      Arrays.asList(
          "star",
          "stars",
          "galaxy",
          "galactic",
          "comet",
          "nebula",
          "planet",
          "moon",
          "lunar",
          "asteroid",
          "meteor",
          "cosmic",
          "space",
          "solar",
          "sun",
          "celestial",
          "universe",
          "supernova",
          "black hole",
          "blackhole",
          "eclipse",
          "astronaut",
          "astro",
          "astral",
          "void",
          "intergalactic",
          "starlight",
          "moonlight");

  private static final Pattern COSMIC_PATTERN;

  static {
    String regex =
        "\\b("
            + String.join(
                "|", COSMIC_TERMS.stream().map(Pattern::quote).toArray(String[]::new))
            + ")\\b";
    COSMIC_PATTERN = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  private boolean required;
  private int minCosmicWords;

  @Override
  public void initialize(CosmicWordCheck constraintAnnotation) {
    this.required = constraintAnnotation.required();
    this.minCosmicWords = constraintAnnotation.minCosmicWords();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return !required;
    }

    log.debug("Validating cosmic words in: '{}'", value);

    var matcher = COSMIC_PATTERN.matcher(value);
    int cosmicWordCount = 0;

    while (matcher.find()) {
      cosmicWordCount++;
      log.debug("Found cosmic term: '{}' in '{}'", matcher.group(), value);

      if (cosmicWordCount >= minCosmicWords) {
        return true;
      }
    }

    if (cosmicWordCount < minCosmicWords) {
      log.debug(
          "Validation failed: found {} cosmic words, required {}", cosmicWordCount, minCosmicWords);

      if (minCosmicWords > 1) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                String.format(
                    "Name must contain at least %d cosmic terms. Found %d cosmic terms.",
                    minCosmicWords, cosmicWordCount))
            .addConstraintViolation();
      }

      return false;
    }

    return true;
  }
}
