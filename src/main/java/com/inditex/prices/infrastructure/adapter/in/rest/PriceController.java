package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.application.usecase.PriceUseCase;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.adapter.in.rest.dto.PriceResponse;
import com.inditex.prices.infrastructure.adapter.in.rest.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/prices")
@RequiredArgsConstructor
@Validated
public class PriceController {
    private final PriceUseCase priceUseCase;

    @Operation(description = "Returns the final price for a given product, brand, and date based on priority rules.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the price",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PriceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input parameters",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Price not found for the given criteria",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public ResponseEntity<PriceResponse> getPrice(
            @Parameter(description = "Request date in format yyyy-MM-dd-HH.mm.ss", example = "2020-06-14-10.00.00",
                    required = true) @RequestParam @NotNull(message = "date is required")
            @DateTimeFormat(pattern = "yyyy-MM-dd-HH.mm.ss") LocalDateTime date,
            @Parameter(description = "Product ID", example = "35455", required = true)
            @RequestParam @NotNull(message = "Product ID is required")
            @Positive(message = "Product ID must be positive") Long productId,
            @Parameter(description = "Brand ID (Company ID)", example = "1", required = true)
            @RequestParam @NotNull(message = "Brand ID is required")
            @Positive(message = "Brand ID must be positive") Long brandId) {
        Price price = priceUseCase.getPrice(date, productId, brandId);
        return ResponseEntity.ok(PriceResponse.fromDomain(price));
    }
}
