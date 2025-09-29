package net.cosmocat.marketplace.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class CosmicWordCheckValidator implements ConstraintValidator<CosmicWordCheck, String> {

    private static final List<String> COSMIC_TERMS = Arrays.asList(
            "star", "stars", "galaxy", "galactic", "comet", "nebula", "planet", "moon", "lunar", "asteroid", "meteor",
            "cosmic", "space", "solar", "sun", "celestial", "universe", "supernova", "black hole", "blackhole",
            "eclipse", "astronaut", "astro", "astral", "void", "intergalactic", "starlight", "moonlight"
    );

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

        String lowerCaseValue = value.toLowerCase();
        int cosmicWordCount = 0;

        for (String cosmicTerm : COSMIC_TERMS) {
            if (containsWholeWord(lowerCaseValue, cosmicTerm)) {
                cosmicWordCount++;
                log.debug("Found cosmic term: '{}' in '{}'", cosmicTerm, value);

                if (cosmicWordCount >= minCosmicWords) {
                    return true;
                }
            }
        }

        if (cosmicWordCount < minCosmicWords) {
            log.debug("Validation failed: found {} cosmic words, required {}", cosmicWordCount, minCosmicWords);

            if (minCosmicWords > 1) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Name must contain at least %d cosmic terms. Found %d cosmic terms.",
                        minCosmicWords, cosmicWordCount)
                ).addConstraintViolation();
            }

            return false;
        }

        return true;
    }

    private boolean containsWholeWord(String text, String word) {
        String pattern = "\\b" + Pattern.quote(word) + "\\b";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }
}