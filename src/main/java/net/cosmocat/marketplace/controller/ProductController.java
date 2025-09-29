package net.cosmocat.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cosmocat.marketplace.database.dto.entity.ProductDTO;
import net.cosmocat.marketplace.database.dto.request.ProductCreateRequest;
import net.cosmocat.marketplace.database.dto.request.ProductUpdateRequest;
import net.cosmocat.marketplace.database.dto.response.CosmoApiResponse;
import net.cosmocat.marketplace.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Management", description = "APIs for managing products in the marketplace")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Get all products",
            description = "Retrieve a list of all available products in the marketplace"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<CosmoApiResponse<List<ProductDTO>>> getAllProducts() {
        log.info("Retrieving all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(CosmoApiResponse.success("Products retrieved successfully", products));
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a specific product by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CosmoApiResponse<ProductDTO>> getProductById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        log.info("Retrieving product with ID: {}", id);
        Optional<ProductDTO> product = productService.getProductById(id);

        return product.map(p -> ResponseEntity.ok(CosmoApiResponse.success("Product found", p)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(CosmoApiResponse.error("Product not found with ID: " + id)));
    }

    @Operation(
            summary = "Create new product",
            description = "Create a new product in the marketplace"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<CosmoApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("Creating new product: {}", request.getName());
        try {
            ProductDTO createdProduct = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CosmoApiResponse.success("Product created successfully", createdProduct));
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CosmoApiResponse.error("Failed to create product: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Update product",
            description = "Update an existing product's information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CosmoApiResponse<ProductDTO>> updateProduct(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Updating product with ID: {}", id);
        try {
            Optional<ProductDTO> updatedProduct = productService.updateProduct(id, request);
            return updatedProduct.map(p -> ResponseEntity.ok(CosmoApiResponse.success("Product updated successfully", p)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(CosmoApiResponse.error("Product not found with ID: " + id)));
        } catch (Exception e) {
            log.error("Error updating product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CosmoApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Delete product",
            description = "Delete a product from the marketplace"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CosmoApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        log.info("Deleting product with ID: {}", id);
        boolean deleted = productService.deleteProduct(id);

        return deleted ? ResponseEntity.noContent().build()
                      : ResponseEntity.status(HttpStatus.NOT_FOUND)
                              .body(CosmoApiResponse.error("Product not found with ID: " + id));
    }

    @Operation(
            summary = "Search products by name",
            description = "Search for products by name (case-insensitive partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = CosmoApiResponse.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<CosmoApiResponse<List<ProductDTO>>> searchProducts(
            @Parameter(description = "Product name to search for", example = "laptop")
            @RequestParam String name) {
        log.info("Searching products by name: {}", name);
        List<ProductDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(CosmoApiResponse.success("Search completed", products));
    }
}