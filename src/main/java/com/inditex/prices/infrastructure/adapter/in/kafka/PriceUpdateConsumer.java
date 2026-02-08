package com.inditex.prices.infrastructure.adapter.in.kafka;

import com.inditex.prices.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to price update events to maintain data consistency across the system.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateConsumer {
    private final PriceRepositoryAdapter priceAdapter;

    /**
     * This method ensures that the distributed cache is cleared when an external price update event is received.
     * @param message The event payload containing product and brand identifiers.
     */
    @KafkaListener(topics = "${kafka.topic-name}", groupId = "price-service-group")
    public void handlePriceUpdate(PriceUpdateMessage message) {
        log.info("Kafka Event: Invalidating cache for product {} due to external update", message.productId());
        priceAdapter.invalidatePrice(message.date(), message.productId(), message.brandId());
    }
}
