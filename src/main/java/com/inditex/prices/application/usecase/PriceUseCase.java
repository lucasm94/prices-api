package com.inditex.prices.application.usecase;

import com.inditex.prices.domain.model.Price;

import java.time.LocalDateTime;

/**
 * Interface for the price search use case.
 */
public interface PriceUseCase {
    Price getPrice(LocalDateTime date, Long productId, Long brandId);
}
