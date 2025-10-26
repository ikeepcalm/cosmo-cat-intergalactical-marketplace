package net.cosmocat.marketplace.database.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryCreateDTO Validation Tests")
class CategoryCreateDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Positive Scenarios - Valid Data")
    class PositiveScenarios {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassWithAllValidFields() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Galactic Products",  // "galactic" is a cosmic word
                    "Products from across the galaxy",
                    List.of("cosmic", "space", "astro")
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with only required fields")
        void shouldPassWithOnlyRequiredFields() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Galaxy Merchandise",
                    null,  // Optional
                    null   // Optional
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with empty optional fields")
        void shouldPassWithEmptyOptionalFields() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    "",                        // Empty description
                    Collections.emptyList()    // Empty tags list
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with minimum name length")
        void shouldPassWithMinimumNameLength() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "AB",  // Minimum 2 characters (may fail cosmic check)
                    null,
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then - May have cosmic word violation but not size violation
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name")
                                     && v.getMessage().contains("size"))
                    .isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with maximum name length")
        void shouldPassWithMaximumNameLength() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "A".repeat(45) + " star",  // 50 characters with cosmic word
                    null,
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with maximum description length")
        void shouldPassWithMaximumDescriptionLength() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    "A".repeat(500),  // Maximum 500 characters
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with maximum number of tags")
        void shouldPassWithMaximumNumberOfTags() {
            // Given
            List<String> maxTags = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                maxTags.add("tag" + i);
            }
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",  // "cosmic" is a cosmic word
                    null,
                    maxTags  // 10 tags (maximum)
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with tags at boundary lengths")
        void shouldPassWithTagsAtBoundaryLengths() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    null,
                    List.of("AB", "A".repeat(20))  // Min 2, max 20 characters
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with various cosmic names")
        void shouldPassWithVariousCosmicNames() {
            // Given
            String[] cosmicNames = {
                    "Star Products",
                    "Galaxy Items",
                    "Cosmic Goods",
                    "Lunar Merchandise"
            };

            for (String name : cosmicNames) {
                CategoryCreateDTO dto = new CategoryCreateDTO(name, null, null);

                // When
                Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .as("Name '%s' should be valid", name)
                        .isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Invalid Data")
    class NegativeScenarios {

        @Test
        @DisplayName("Should fail validation when name is null")
        void shouldFailWhenNameIsNull() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    null,  // Invalid: null name
                    "Description",
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("name");
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailWhenNameIsBlank() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "   ",  // Invalid: blank name
                    "Description",
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("name");
        }

        @Test
        @DisplayName("Should fail validation when name is too short")
        void shouldFailWhenNameIsTooShort() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "S",  // Invalid: only 1 character, minimum is 2
                    null,
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when name is too long")
        void shouldFailWhenNameIsTooLong() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "A".repeat(51),  // Invalid: 51 characters, maximum is 50
                    null,
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("50"));
        }

        @Test
        @DisplayName("Should fail validation when name lacks cosmic words")
        void shouldFailWhenNameLacksCosmicWords() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Regular Category",  // Invalid: no cosmic words
                    null,
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("cosmic"));
        }

        @Test
        @DisplayName("Should fail validation when description exceeds maximum length")
        void shouldFailWhenDescriptionExceedsMaxLength() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    "A".repeat(501),  // Invalid: 501 characters, maximum is 500
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("description"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("500"));
        }

        @Test
        @DisplayName("Should fail validation when tags list exceeds maximum size")
        void shouldFailWhenTagsExceedMaximumSize() {
            // Given
            List<String> tooManyTags = new ArrayList<>();
            for (int i = 0; i < 11; i++) {  // 11 tags, maximum is 10
                tooManyTags.add("tag" + i);
            }
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Stellar Category",
                    null,
                    tooManyTags
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("tags"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("10"));
        }

        @Test
        @DisplayName("Should fail validation when tag is too short")
        void shouldFailWhenTagIsTooShort() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    null,
                    List.of("A")  // Invalid: only 1 character, minimum is 2
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().contains("tags"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when tag is too long")
        void shouldFailWhenTagIsTooLong() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    null,
                    List.of("A".repeat(21))  // Invalid: 21 characters, maximum is 20
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().contains("tags"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("20"));
        }

        @Test
        @DisplayName("Should fail validation when tag is blank")
        void shouldFailWhenTagIsBlank() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    null,
                    List.of("   ")  // Invalid: blank tag
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().contains("tags"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when multiple tags are invalid")
        void shouldFailWhenMultipleTagsAreInvalid() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    null,
                    List.of("A", "B", "")  // All invalid (too short or empty)
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().contains("tags"))
                    .hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle multiple validation failures simultaneously")
        void shouldHandleMultipleValidationFailures() {
            // Given - DTO with multiple invalid fields
            List<String> invalidTags = new ArrayList<>();
            for (int i = 0; i < 11; i++) {  // Too many tags
                invalidTags.add("X");  // Each tag too short
            }
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "A",  // Too short, no cosmic word
                    "A".repeat(501),  // Description too long
                    invalidTags
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should validate maximum boundary values")
        void shouldValidateMaximumBoundaryValues() {
            // Given
            List<String> maxTags = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                maxTags.add("A".repeat(20));  // Maximum 20 characters each
            }
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "A".repeat(45) + " star",  // 50 characters with cosmic word
                    "B".repeat(500),           // Maximum 500 characters
                    maxTags                     // Maximum 10 tags
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in name")
        void shouldHandleSpecialCharactersInName() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic & Stellar Products!",  // Special characters with cosmic word
                    null,
                    null
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then - Should pass as long as size and cosmic word requirements are met
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass with case-insensitive cosmic words")
        void shouldPassWithCaseInsensitiveCosmicWords() {
            // Given
            String[] cosmicNames = {
                    "STAR products",
                    "galaxy ITEMS",
                    "CoSmIc goods"
            };

            for (String name : cosmicNames) {
                CategoryCreateDTO dto = new CategoryCreateDTO(name, null, null);

                // When
                Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .as("Name '%s' should pass cosmic word validation", name)
                        .filteredOn(v -> v.getMessage().contains("cosmic"))
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should validate mixed valid and invalid tags")
        void shouldValidateMixedTags() {
            // Given
            CategoryCreateDTO dto = new CategoryCreateDTO(
                    "Cosmic Category",
                    null,
                    List.of(
                            "validTag1",      // Valid
                            "AB",             // Valid (minimum)
                            "A",              // Invalid (too short)
                            "A".repeat(21)    // Invalid (too long)
                    )
            );

            // When
            Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().contains("tags"))
                    .hasSizeGreaterThanOrEqualTo(2);
        }
    }
}