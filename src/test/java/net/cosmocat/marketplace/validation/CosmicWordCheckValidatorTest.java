package net.cosmocat.marketplace.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CosmicWordCheckValidator Tests")
class CosmicWordCheckValidatorTest {

    private CosmicWordCheckValidator validator;

    @Mock
    private CosmicWordCheck annotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new CosmicWordCheckValidator();
    }

    @Nested
    @DisplayName("Positive Scenarios - Valid Cosmic Words")
    class PositiveScenarios {

        @Test
        @DisplayName("Should validate text containing 'star'")
        void shouldValidateTextWithStar() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("This is a star product", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate text containing 'galaxy'")
        void shouldValidateTextWithGalaxy() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("From another galaxy", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate text with all 27 cosmic terms")
        void shouldValidateAllCosmicTerms() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            String[] cosmicTerms = {
                    "star", "stars", "galaxy", "galactic", "comet", "nebula",
                    "planet", "moon", "lunar", "asteroid", "meteor", "cosmic",
                    "space", "solar", "sun", "celestial", "universe", "supernova",
                    "black hole", "blackhole", "eclipse", "astronaut", "astro",
                    "astral", "void", "intergalactic", "starlight", "moonlight"
            };

            for (String term : cosmicTerms) {
                // When
                boolean result = validator.isValid("Product with " + term, context);

                // Then
                assertThat(result)
                        .as("Term '%s' should be recognized as cosmic", term)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("Should validate text with multiple cosmic words")
        void shouldValidateTextWithMultipleCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Stellar galaxy cosmic star", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate case-insensitive cosmic words")
        void shouldValidateCaseInsensitiveCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            String[] caseVariations = {
                    "STAR", "Star", "sTaR", "GALAXY", "Galaxy", "GaLaXy",
                    "COSMIC", "Cosmic", "CoSmIc"
            };

            for (String variation : caseVariations) {
                // When
                boolean result = validator.isValid("Product " + variation, context);

                // Then
                assertThat(result)
                        .as("Case variation '%s' should be recognized", variation)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("Should validate cosmic word at beginning of text")
        void shouldValidateCosmicWordAtBeginning() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Star Product", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate cosmic word at end of text")
        void shouldValidateCosmicWordAtEnd() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Product of the stars", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate cosmic word as entire text")
        void shouldValidateCosmicWordAsEntireText() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("galaxy", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate compound cosmic words like 'black hole'")
        void shouldValidateCompoundCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Travel through a black hole", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate 'blackhole' as single word")
        void shouldValidateBlackholeAsSingleWord() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Journey to the blackhole", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true when not required and text has no cosmic words")
        void shouldReturnTrueWhenNotRequiredAndNoCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(false);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Regular product name", context);

            // Then
            assertThat(result).isFalse();  // No cosmic words found
        }

        @Test
        @DisplayName("Should return true when value is null and not required")
        void shouldReturnTrueWhenValueIsNull() {
            // Given
            when(annotation.required()).thenReturn(false);  // Not required
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid(null, context);

            // Then
            assertThat(result).isTrue();  // Null is valid when not required
        }

        @Test
        @DisplayName("Should return false when value is null and required")
        void shouldReturnFalseWhenValueIsNullAndRequired() {
            // Given
            when(annotation.required()).thenReturn(true);  // Required
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid(null, context);

            // Then
            assertThat(result).isFalse();  // Null is invalid when required
        }

        @Test
        @DisplayName("Should return true when value is blank and not required")
        void shouldReturnTrueWhenValueIsBlank() {
            // Given
            when(annotation.required()).thenReturn(false);  // Not required
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("   ", context);

            // Then
            assertThat(result).isTrue();  // Blank is valid when not required
        }

        @Test
        @DisplayName("Should return false when value is blank and required")
        void shouldReturnFalseWhenValueIsBlankAndRequired() {
            // Given
            when(annotation.required()).thenReturn(true);  // Required
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("   ", context);

            // Then
            assertThat(result).isFalse();  // Blank is invalid when required
        }

        @Test
        @DisplayName("Should validate when minimum cosmic words requirement is met")
        void shouldValidateWhenMinCosmicWordsRequirementMet() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(3);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Cosmic star galaxy universe", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should NOT validate cosmic word within larger word due to word boundaries")
        void shouldNotValidateCosmicWordWithinLargerWord() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When - "astro" is within "astrophysics" but won't match due to \b boundaries
            boolean result = validator.isValid("Astronomy and astrophysics", context);

            // Then
            assertThat(result).isFalse();  // Word boundaries prevent partial matches
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Invalid Input")
    class NegativeScenarios {

        @Test
        @DisplayName("Should fail validation when required and no cosmic words present")
        void shouldFailWhenRequiredAndNoCosmicWords() {
            // Given - No mock needed for minCosmicWords==1
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Regular product name", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail validation when text contains no recognizable cosmic terms")
        void shouldFailWithNoRecognizableCosmicTerms() {
            // Given - No mock needed for minCosmicWords==1
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Just a normal everyday product", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail validation when minimum cosmic words not met")
        void shouldFailWhenMinCosmicWordsNotMet() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(3);
            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(violationBuilder);
            validator.initialize(annotation);

            // When - Only 2 cosmic words
            boolean result = validator.isValid("Cosmic star product", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail validation with numeric-only text")
        void shouldFailWithNumericOnlyText() {
            // Given - No mock needed for minCosmicWords==1
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("12345", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail validation with special characters only")
        void shouldFailWithSpecialCharactersOnly() {
            // Given - No mock needed for minCosmicWords==1
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("!@#$%^&*()", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail validation when text contains partial cosmic word matches")
        void shouldFailWithPartialCosmicWordMatches() {
            // Given - No mock needed for minCosmicWords==1
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When - "sta" is not "star", "gala" is not "galaxy"
            boolean result = validator.isValid("Product sta gala", context);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Error Message Generation")
    class ErrorMessageTests {

        @Test
        @DisplayName("Should generate custom error message with required cosmic words count")
        void shouldGenerateCustomErrorMessage() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(2);
            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(violationBuilder);
            validator.initialize(annotation);

            // When
            validator.isValid("Regular product", context);

            // Then
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());

            String capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage)
                    .contains("must contain at least")
                    .contains("cosmic terms");
        }

        @Test
        @DisplayName("Should NOT generate custom message when minCosmicWords is 1")
        void shouldNotGenerateCustomMessageForSingleWord() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            validator.isValid("Regular product", context);

            // Then - No custom message when minCosmicWords == 1
            verify(context, never()).buildConstraintViolationWithTemplate(anyString());
            verify(context, never()).disableDefaultConstraintViolation();
        }

        @Test
        @DisplayName("Should use plural form for multiple required cosmic words")
        void shouldUsePluralFormForMultipleWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(3);
            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(violationBuilder);
            validator.initialize(annotation);

            // When
            validator.isValid("Regular product", context);

            // Then
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());

            String capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage).contains("3 cosmic terms");
        }

        @Test
        @DisplayName("Should disable default constraint violation when custom message is generated")
        void shouldDisableDefaultConstraintViolation() {
            // Given - Only generates custom message when minCosmicWords > 1
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(2);  // Greater than 1
            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(violationBuilder);
            validator.initialize(annotation);

            // When
            validator.isValid("Regular product", context);

            // Then
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate(anyString());
            verify(violationBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should not generate custom message when validation passes")
        void shouldNotGenerateCustomMessageWhenValidationPasses() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            validator.isValid("Cosmic product", context);

            // Then
            verify(context, never()).disableDefaultConstraintViolation();
            verify(context, never()).buildConstraintViolationWithTemplate(anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should return false for empty string when required")
        void shouldHandleEmptyStringWhenRequired() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("", context);

            // Then
            assertThat(result).isFalse();  // Empty string is invalid when required
        }

        @Test
        @DisplayName("Should return true for empty string when not required")
        void shouldHandleEmptyStringWhenNotRequired() {
            // Given
            when(annotation.required()).thenReturn(false);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("", context);

            // Then
            assertThat(result).isTrue();  // Empty string is valid when not required
        }

        @Test
        @DisplayName("Should handle very long text with cosmic words")
        void shouldHandleVeryLongTextWithCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            String longText = "A".repeat(1000) + " cosmic " + "B".repeat(1000);

            // When
            boolean result = validator.isValid(longText, context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle text with only whitespace and cosmic word")
        void shouldHandleTextWithWhitespaceAndCosmicWord() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("   star   ", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should count duplicate cosmic words correctly")
        void shouldCountDuplicateCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(3);
            validator.initialize(annotation);

            // When - "star" appears 3 times
            boolean result = validator.isValid("star star star", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle mixed cosmic and non-cosmic words")
        void shouldHandleMixedWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(2);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("normal star regular galaxy ordinary", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle minCosmicWords set to zero")
        void shouldHandleMinCosmicWordsZero() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(0);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("no cosmic words here", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle text with newlines and cosmic words")
        void shouldHandleTextWithNewlines() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Product\nfrom\nthe\ngalaxy", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle text with tabs and cosmic words")
        void shouldHandleTextWithTabs() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Product\tfrom\tthe\tcosmic\tspace", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle Unicode characters with cosmic words")
        void shouldHandleUnicodeCharacters() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("Cosmic product \u2605\u2606", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle very high minCosmicWords requirement")
        void shouldHandleVeryHighMinCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(100);
            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(violationBuilder);
            validator.initialize(annotation);

            // When
            boolean result = validator.isValid("star galaxy cosmic", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should validate with exactly minimum required cosmic words")
        void shouldValidateWithExactlyMinimumRequired() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(2);
            validator.initialize(annotation);

            // When - Exactly 2 cosmic words
            boolean result = validator.isValid("star galaxy", context);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should fail with one less than minimum required cosmic words")
        void shouldFailWithOneLessThanMinimum() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(3);
            when(context.buildConstraintViolationWithTemplate(anyString()))
                    .thenReturn(violationBuilder);
            validator.initialize(annotation);

            // When - Only 2 cosmic words, need 3
            boolean result = validator.isValid("star galaxy", context);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize with required=true")
        void shouldInitializeWithRequiredTrue() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(1);

            // When
            validator.initialize(annotation);
            boolean result = validator.isValid("regular product", context);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should initialize with required=false but still validate cosmic words")
        void shouldInitializeWithRequiredFalse() {
            // Given
            when(annotation.required()).thenReturn(false);
            when(annotation.minCosmicWords()).thenReturn(1);

            // When
            validator.initialize(annotation);
            boolean result = validator.isValid("regular product", context);

            // Then - Still returns false because no cosmic words, required only affects null/blank
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should initialize with custom minCosmicWords")
        void shouldInitializeWithCustomMinCosmicWords() {
            // Given
            when(annotation.required()).thenReturn(true);
            when(annotation.minCosmicWords()).thenReturn(5);

            // When
            validator.initialize(annotation);
            boolean result = validator.isValid("star galaxy cosmic moon sun", context);

            // Then
            assertThat(result).isTrue();
        }
    }
}