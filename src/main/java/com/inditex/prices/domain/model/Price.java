package com.inditex.prices.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a product price.
 */
@Builder
public record Price(
        Long brandId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer priceList,
        Long productId,
        Integer priority,
        BigDecimal price,
        String currency
) {}
