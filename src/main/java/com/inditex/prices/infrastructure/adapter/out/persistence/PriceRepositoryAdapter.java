package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.domain.exception.NotFoundException;
import com.inditex.prices.domain.exception.ServiceUnavailableException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.repository.PriceRepository;
import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.inditex.prices.infrastructure.monitoring.MetricsEndpoint.PRICE_DETAIL;

/**
 * Adapter that connects the domain port with the JPA infrastructure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceRepositoryAdapter implements PriceRepository {
    private final JpaPriceRepository jpaPriceRepository;
    private final MetricsRecorder metrics;

    /**
     * Retrieves the price from the primary persistence store and caches the result
     * @param date      The date.
     * @param productId The product ID.
     * @param brandId   The brand ID.
     * @return The Price found in the database.
     * @throws NotFoundException if price is not found for the given criteria.
     */
    @Override
    @CircuitBreaker(name = "pricesSearch", fallbackMethod = "handleGetPriceFailure")
    @Cacheable(value = "priceDetail", key = "{#date, #productId, #brandId}")
    public Price getPrice(LocalDateTime date, Long productId, Long brandId) {
        log.info("Search product: {} - on Database", productId);
        metrics.recordRequest(PRICE_DETAIL.getValue(), MetricsType.DATABASE_FETCH);
        return jpaPriceRepository.findTopPrice(date, productId, brandId).map(PriceEntity::toDomain)
                .orElseThrow(() -> new NotFoundException("Price not found for product"));
    }

    /**
     * Fallback method for the price search operation.
     * Triggered when a technical failure occurs or the circuit is open.
     *
     * @param date      The date.
     * @param productId The product ID.
     * @param brandId   The brand ID.
     * @param t         The exception that triggered the fallback.
     * @throws ServiceUnavailableException indicating technical service failure.
     */
    public Price handleGetPriceFailure(LocalDateTime date, Long productId, Long brandId, Throwable t) {
        if (t instanceof NotFoundException) throw (NotFoundException) t;

        log.error("Circuit breaker 'pricesSearch' triggered. Technical failure: {}", t.getMessage());
        metrics.recordRequest(PRICE_DETAIL.getValue(), MetricsType.FALLBACK);
        throw new ServiceUnavailableException("Service unavailable. Please try again later.");
    }

    /**
     * Method for cache invalidation.
     */
    @CacheEvict(value = "priceDetail", key = "{#date, #productId, #brandId}")
    public void invalidatePrice(LocalDateTime date, Long productId, Long brandId) {
        log.info("Cache invalidated for product: {} - brand: {} - date: {}", productId, brandId, date);
        metrics.recordRequest(PRICE_DETAIL.getValue(), MetricsType.CACHE_INVALIDATION);
    }

}
