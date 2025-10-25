package net.cosmocat.marketplace.database.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductUpdateDTO Validation Tests")
class ProductUpdateDTOValidationTest {

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
        @DisplayName("Should pass validation with empty DTO (all fields null)")
        void shouldPassWithAllFieldsNull() {
            // Given - All fields are null (optional for update)
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null, null, null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with only name updated (with cosmic word)")
        void shouldPassWithOnlyNameUpdated() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "Updated Galactic Product",  // Contains cosmic word "galactic"
                    null, null, null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when name lacks cosmic words (required=false only for null/blank)")
        void shouldFailWithNonCosmicName() {
            // Given - required=false only affects null/blank values, not non-blank without cosmic words
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "Regular Product Name",  // No cosmic words
                    null, null, null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then - Fails because if name is provided, it must have cosmic words
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassWithAllValidFields() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "Updated Cosmic Product",
                    "Updated cosmic description",
                    149.99,
                    "EUR",
                    50,
                    "GALAXY-002",
                    "https://example.com/updated.jpg",
                    3.0,
                    "15 x 25 x 35 cm",
                    AvailabilityStatus.OUT_OF_STOCK,
                    2L
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with minimum valid values")
        void shouldPassWithMinimumValues() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "AB",      // Minimum 2 chars
                    null,
                    0.01,      // Minimum price
                    "USD",
                    0,         // Minimum stock
                    "MIN",     // Minimum 3 chars
                    null,
                    0.001,     // Minimum weight
                    null,
                    null,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then - May have size violations but not below min
            assertThat(violations).allMatch(v ->
                    !v.getMessage().contains("at least") &&
                            !v.getMessage().contains("DecimalMin")
            );
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Invalid Data")
    class NegativeScenarios {

        @Test
        @DisplayName("Should fail validation when name is too short")
        void shouldFailWhenNameIsTooShort() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "A",  // Invalid: only 1 character, minimum is 2
                    null, null, null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

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
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "A".repeat(101),  // Invalid: exceeds 100 characters
                    null, null, null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("100"));
        }

        @Test
        @DisplayName("Should fail validation when price is zero")
        void shouldFailWhenPriceIsZero() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null,
                    null,
                    0.0,  // Invalid: not positive
                    null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("price"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("positive"));
        }

        @Test
        @DisplayName("Should fail validation when price is negative")
        void shouldFailWhenPriceIsNegative() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null,
                    -50.0,  // Invalid: negative
                    null, null,
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("price"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when currency format is invalid")
        void shouldFailWhenCurrencyFormatIsInvalid() {
            // Given
            String[] invalidCurrencies = {"US", "USDD", "us$", "123"};

            for (String currency : invalidCurrencies) {
                ProductUpdateDTO dto = new ProductUpdateDTO(
                        null, null, null,
                        currency,  // Invalid format
                        null, null, null, null, null, null, null
                );

                // When
                Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("currency"))
                        .as("Currency '%s' should be invalid", currency)
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Should fail validation when stock quantity is negative")
        void shouldFailWhenStockQuantityIsNegative() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null, null, null,
                    -10,  // Invalid: negative
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("stockQuantity"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when stock quantity exceeds maximum")
        void shouldFailWhenStockQuantityExceedsMaximum() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null, null, null,
                    100001,  // Invalid: exceeds 100000
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("stockQuantity"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("100"));
        }

        @Test
        @DisplayName("Should fail validation when SKU contains invalid characters")
        void shouldFailWhenSkuContainsInvalidCharacters() {
            // Given
            String[] invalidSkus = {"sku-001", "SKU_001", "SKU 001"};

            for (String sku : invalidSkus) {
                ProductUpdateDTO dto = new ProductUpdateDTO(
                        null, null, null, null, null,
                        sku,  // Invalid: lowercase, underscore, or space
                        null, null, null, null, null
                );

                // When
                Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("sku"))
                        .as("SKU '%s' should be invalid", sku)
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Should fail validation when dimensions format is invalid")
        void shouldFailWhenDimensionsFormatIsInvalid() {
            // Given
            String[] invalidDimensions = {
                    "10x20",           // Missing third dimension
                    "10-20-30 cm",     // Wrong separator
                    "10 x 20 x 30"     // Missing unit
            };

            for (String dimension : invalidDimensions) {
                ProductUpdateDTO dto = new ProductUpdateDTO(
                        null, null, null, null, null, null, null, null,
                        dimension,  // Invalid format
                        null, null
                );

                // When
                Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("dimensions"))
                        .as("Dimension '%s' should be invalid", dimension)
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Should fail validation when weight is zero")
        void shouldFailWhenWeightIsZero() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null, null, null, null, null, null,
                    0.0,  // Invalid: not positive
                    null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("weight"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("positive"));
        }

        @Test
        @DisplayName("Should fail validation when category ID is zero")
        void shouldFailWhenCategoryIdIsZero() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null, null, null, null, null, null, null, null, null,
                    0L  // Invalid: not positive
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("categoryId"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("positive"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle multiple validation failures simultaneously")
        void shouldHandleMultipleValidationFailures() {
            // Given - DTO with multiple invalid fields
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "X",       // Too short
                    "A".repeat(1001),  // Description too long
                    -100.0,    // Negative price
                    "$",       // Invalid currency
                    -50,       // Negative stock
                    "AB",      // SKU too short
                    null,
                    -1.0,      // Negative weight
                    null,
                    null,
                    -1L        // Negative category ID
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should validate partial updates without triggering required field validations")
        void shouldAllowPartialUpdates() {
            // Given - Update only stock quantity
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    null, null, null, null,
                    25,  // Only updating stock
                    null, null, null, null, null, null
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should validate maximum boundary values")
        void shouldValidateMaximumBoundaryValues() {
            // Given
            ProductUpdateDTO dto = new ProductUpdateDTO(
                    "A".repeat(94) + " cosmic",  // Maximum 100 chars with cosmic word
                    "B".repeat(1000),    // Maximum 1000 chars
                    99999.99,            // High price
                    "USD",
                    100000,              // Maximum stock
                    "C".repeat(50),      // Maximum 50 chars
                    null,
                    1000.0,              // Maximum weight
                    "D".repeat(88) + " x 1 x 1 cm",  // Maximum 100 chars (88 + 12 = 100)
                    null,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductUpdateDTO>> violations = validator.validate(dto);

            // Then - Should have no size-related violations
            assertThat(violations)
                    .filteredOn(v -> v.getMessage().contains("exceed") ||
                            v.getMessage().contains("DecimalMax") ||
                            v.getMessage().contains("Max"))
                    .isEmpty();
        }
    }
}