package com.inditex.prices.domain.repository;

import com.inditex.prices.domain.model.Price;

import java.time.LocalDateTime;

/**
 * Output port for price persistence operations.
 */
public interface PriceRepository {
    Price getPrice(LocalDateTime date, Long productId, Long brandId);
}
