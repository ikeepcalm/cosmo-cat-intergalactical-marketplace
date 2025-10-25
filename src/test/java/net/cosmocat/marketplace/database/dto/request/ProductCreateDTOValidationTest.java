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

@DisplayName("ProductCreateDTO Validation Tests")
class ProductCreateDTOValidationTest {

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
        @DisplayName("Should pass validation with all valid required fields")
        void shouldPassWithAllValidRequiredFields() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Star Widget",
                    "A stellar product from another galaxy",
                    99.99,
                    "USD",
                    100,
                    "STAR-001",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with minimum valid price")
        void shouldPassWithMinimumPrice() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Star",
                    null,
                    0.01,  // Minimum price
                    "USD",
                    0,
                    "STAR-MIN",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with maximum valid price")
        void shouldPassWithMaximumPrice() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Galaxy Product",
                    null,
                    999999.99,  // Maximum price
                    "USD",
                    0,
                    "STAR-MAX",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with all optional fields populated")
        void shouldPassWithAllOptionalFieldsPopulated() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Star Widget",
                    "A stellar product",
                    99.99,
                    "USD",
                    100,
                    "STAR-001",
                    "https://example.com/cosmic-star.jpg",
                    2.5,
                    "10 x 20 x 30 cm",
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with valid currency codes")
        void shouldPassWithValidCurrencyCodes() {
            // Given
            String[] validCurrencies = {"USD", "EUR", "GBP", "JPY", "AUD"};

            for (String currency : validCurrencies) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Stellar Product",
                        null,
                        99.99,
                        currency,
                        10,
                        "TEST-123",
                        null,
                        null,
                        null,
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("currency"))
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should pass validation with valid SKU formats")
        void shouldPassWithValidSkuFormats() {
            // Given
            String[] validSkus = {"STAR-001", "GALAXY123", "ABC-XYZ-999", "COSMIC"};

            for (String sku : validSkus) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Cosmic Item",
                        null,
                        50.0,
                        "USD",
                        10,
                        sku,
                        null,
                        null,
                        null,
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("sku"))
                        .as("SKU '%s' should be valid", sku)
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should pass validation with valid dimension formats")
        void shouldPassWithValidDimensionFormats() {
            // Given
            String[] validDimensions = {
                    "10 x 20 x 30 cm",
                    "5 x 5 x 5 mm",
                    "1.5 x 2.5 x 3.5 m",
                    "10x20x30 inch"
            };

            for (String dimension : validDimensions) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Cosmic Widget",
                        null,
                        50.0,
                        "USD",
                        10,
                        "TEST-DIM",
                        null,
                        null,
                        dimension,
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("dimensions"))
                        .as("Dimension '%s' should be valid", dimension)
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should pass validation with minimum stock quantity of zero")
        void shouldPassWithMinimumStockQuantity() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Star Product",
                    null,
                    10.0,
                    "USD",
                    0,  // Minimum stock quantity
                    "STAR-ZERO",
                    null,
                    null,
                    null,
                    AvailabilityStatus.OUT_OF_STOCK,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with maximum stock quantity")
        void shouldPassWithMaximumStockQuantity() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Galaxy Product",
                    null,
                    10.0,
                    "USD",
                    100000,  // Maximum stock quantity
                    "STAR-MAX",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Name Field Validation")
    class NameValidationTests {

        @Test
        @DisplayName("Should fail validation when name is null")
        void shouldFailWhenNameIsNull() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    null,  // Invalid: null name
                    "Description",
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

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
            ProductCreateDTO dto = new ProductCreateDTO(
                    "   ",  // Invalid: blank name
                    "Description",
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

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
            ProductCreateDTO dto = new ProductCreateDTO(
                    "S",  // Invalid: only 1 character, minimum is 2
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

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
            ProductCreateDTO dto = new ProductCreateDTO(
                    "A".repeat(101),  // Invalid: 101 characters, maximum is 100
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("100"));
        }

        @Test
        @DisplayName("Should fail validation when name lacks cosmic words")
        void shouldFailWhenNameLacksCosmicWords() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Regular Widget Product",  // Invalid: no cosmic words
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("cosmic"));
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Price Field Validation")
    class PriceValidationTests {

        @Test
        @DisplayName("Should fail validation when price is null")
        void shouldFailWhenPriceIsNull() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    null,  // Invalid: null price
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("price");
        }

        @Test
        @DisplayName("Should fail validation when price is zero")
        void shouldFailWhenPriceIsZero() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    0.0,  // Invalid: zero price
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

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
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    -10.0,  // Invalid: negative price
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("price"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when price exceeds maximum")
        void shouldFailWhenPriceExceedsMaximum() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    10000000.0,  // Invalid: far exceeds maximum
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("price"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when price is below minimum")
        void shouldFailWhenPriceIsBelowMinimum() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    0.001,  // Invalid: below 0.01
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("price"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("0.01"));
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Currency Field Validation")
    class CurrencyValidationTests {

        @Test
        @DisplayName("Should fail validation when currency is null")
        void shouldFailWhenCurrencyIsNull() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    null,  // Invalid: null currency
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("currency");
        }

        @Test
        @DisplayName("Should fail validation when currency format is invalid")
        void shouldFailWhenCurrencyFormatIsInvalid() {
            // Given
            String[] invalidCurrencies = {"US", "USDD", "us$", "123", "U D"};

            for (String currency : invalidCurrencies) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Cosmic Product",
                        null,
                        99.99,
                        currency,  // Invalid currency format
                        10,
                        "TEST-SKU",
                        null,
                        null,
                        null,
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("currency"))
                        .as("Currency '%s' should be invalid", currency)
                        .isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Stock Quantity Field Validation")
    class StockQuantityValidationTests {

        @Test
        @DisplayName("Should fail validation when stock quantity is null")
        void shouldFailWhenStockQuantityIsNull() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    null,  // Invalid: null stock quantity
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("stockQuantity");
        }

        @Test
        @DisplayName("Should fail validation when stock quantity is negative")
        void shouldFailWhenStockQuantityIsNegative() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    -1,  // Invalid: negative stock quantity
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

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
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    100001,  // Invalid: exceeds 100000
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("stockQuantity"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("100") || msg.contains("100,000"));
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - SKU Field Validation")
    class SkuValidationTests {

        @Test
        @DisplayName("Should fail validation when SKU is too short")
        void shouldFailWhenSkuIsTooShort() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "AB",  // Invalid: only 2 characters, minimum is 3
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("sku"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should fail validation when SKU is too long")
        void shouldFailWhenSkuIsTooLong() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "A".repeat(51),  // Invalid: 51 characters, maximum is 50
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("sku"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("50"));
        }

        @Test
        @DisplayName("Should fail validation when SKU contains invalid characters")
        void shouldFailWhenSkuContainsInvalidCharacters() {
            // Given
            String[] invalidSkus = {"sku-001", "SKU_001", "SKU 001", "SKU@001"};

            for (String sku : invalidSkus) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Cosmic Product",
                        null,
                        99.99,
                        "USD",
                        10,
                        sku,  // Invalid: contains lowercase, underscore, space, or special chars
                        null,
                        null,
                        null,
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("sku"))
                        .as("SKU '%s' should be invalid", sku)
                        .isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Dimensions Field Validation")
    class DimensionsValidationTests {

        @Test
        @DisplayName("Should fail validation when dimensions format is invalid")
        void shouldFailWhenDimensionsFormatIsInvalid() {
            // Given
            String[] invalidDimensions = {
                    "10x20",                  // Missing third dimension
                    "10-20-30 cm",            // Wrong separator
                    "axbxc cm",               // Non-numeric
                    "10 x 20 x 30"            // Missing unit
            };

            for (String dimension : invalidDimensions) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Cosmic Product",
                        null,
                        99.99,
                        "USD",
                        10,
                        "TEST-DIM",
                        null,
                        null,
                        dimension,  // Invalid dimension format
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("dimensions"))
                        .as("Dimension '%s' should be invalid", dimension)
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Should fail validation when dimensions exceed maximum length")
        void shouldFailWhenDimensionsExceedMaxLength() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-DIM",
                    null,
                    null,
                    "1".repeat(101),  // Invalid: exceeds 100 characters
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("dimensions"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("100"));
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Availability Status Field Validation")
    class AvailabilityStatusValidationTests {

        @Test
        @DisplayName("Should fail validation when availability status is null")
        void shouldFailWhenAvailabilityStatusIsNull() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    null,  // Invalid: null availability status
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("availabilityStatus");
        }
    }

    @Nested
    @DisplayName("Negative Scenarios - Category ID Field Validation")
    class CategoryIdValidationTests {

        @Test
        @DisplayName("Should fail validation when category ID is zero")
        void shouldFailWhenCategoryIdIsZero() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    0L  // Invalid: zero category ID
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("categoryId"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("positive"));
        }

        @Test
        @DisplayName("Should fail validation when category ID is negative")
        void shouldFailWhenCategoryIdIsNegative() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    -1L  // Invalid: negative category ID
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("categoryId"))
                    .isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle multiple validation failures simultaneously")
        void shouldHandleMultipleValidationFailures() {
            // Given - DTO with multiple invalid fields
            ProductCreateDTO dto = new ProductCreateDTO(
                    "A",         // Too short, no cosmic word
                    null,
                    -1.0,        // Negative price
                    "US",        // Invalid currency format
                    -5,          // Negative stock
                    "AB",        // Too short SKU
                    null,
                    null,
                    null,
                    null,        // Null availability status
                    -1L          // Negative category ID
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should validate description field length limit")
        void shouldValidateDescriptionLength() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    "A".repeat(1001),  // Invalid: exceeds 1000 characters
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    null,
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("description"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("1000"));
        }

        @Test
        @DisplayName("Should fail validation when image URL format is invalid")
        void shouldFailWhenImageUrlFormatIsInvalid() {
            // Given
            String[] invalidUrls = {"not-a-url", "ftp://example.com/image.jpg"};

            for (String url : invalidUrls) {
                ProductCreateDTO dto = new ProductCreateDTO(
                        "Cosmic Product",
                        null,
                        99.99,
                        "USD",
                        10,
                        "TEST-SKU",
                        url,  // Invalid URL
                        null,
                        null,
                        AvailabilityStatus.AVAILABLE,
                        1L
                );

                // When
                Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

                // Then
                assertThat(violations)
                        .filteredOn(v -> v.getPropertyPath().toString().equals("image"))
                        .as("URL '%s' should be invalid", url)
                        .isNotEmpty();
            }
        }

        @Test
        @DisplayName("Should fail validation when weight is zero")
        void shouldFailWhenWeightIsZero() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    0.0,  // Invalid: zero weight
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("weight"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("positive"));
        }

        @Test
        @DisplayName("Should fail validation when weight exceeds maximum")
        void shouldFailWhenWeightExceedsMaximum() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    1001.0,  // Invalid: exceeds 1000.0
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("weight"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("1000"));
        }

        @Test
        @DisplayName("Should fail validation when weight is below minimum")
        void shouldFailWhenWeightIsBelowMinimum() {
            // Given
            ProductCreateDTO dto = new ProductCreateDTO(
                    "Cosmic Product",
                    null,
                    99.99,
                    "USD",
                    10,
                    "TEST-SKU",
                    null,
                    0.0001,  // Invalid: below 0.001
                    null,
                    AvailabilityStatus.AVAILABLE,
                    1L
            );

            // When
            Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("weight"))
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("0.001"));
        }
    }
}