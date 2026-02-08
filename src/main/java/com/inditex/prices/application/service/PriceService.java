package com.inditex.prices.application.service;

import com.inditex.prices.application.usecase.PriceUseCase;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.repository.PriceRepository;
import com.inditex.prices.infrastructure.monitoring.MetricsEndpoint;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service implementation for product prices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService implements PriceUseCase {
    private final PriceRepository priceRepository;
    private final MetricsRecorder metrics;

    /**
     * Executes the search for a price based on criteria.
     *
     * @param date      The date.
     * @param productId The product identifier.
     * @param brandId   The brand identifier.
     * @return The found price.
     * @throws NullPointerException if the date is null.
     */
    public Price getPrice(LocalDateTime date, Long productId, Long brandId) {
        log.info("Getting a price based on product id: {} - brand id: {} - date: {}", productId, brandId, date);
        Objects.requireNonNull(date, "The date must not be null");
        Price price = priceRepository.getPrice(date, productId, brandId);
        log.info("Returning price by product id {}", productId);
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.SUCCESS);
        return price;
    }

}
