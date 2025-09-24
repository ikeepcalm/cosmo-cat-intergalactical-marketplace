package net.cosmocat.marketplace.database.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard API response wrapper")
public class CosmoApiResponse<T> {

    @Schema(description = "Response status", example = "success")
    private String status;

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    public static <T> CosmoApiResponse<T> success(T data) {
        return new CosmoApiResponse<>("success", "Operation completed successfully", data, LocalDateTime.now());
    }

    public static <T> CosmoApiResponse<T> success(String message, T data) {
        return new CosmoApiResponse<>("success", message, data, LocalDateTime.now());
    }

    public static <T> CosmoApiResponse<T> error(String message) {
        return new CosmoApiResponse<>("error", message, null, LocalDateTime.now());
    }
}