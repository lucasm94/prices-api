package com.inditex.prices.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Object representing a price update event message.
 * Used for asynchronous communication and distributed cache invalidation.
 */
@Builder
public record PriceUpdateMessage(
        Long productId,
        Long brandId,
        @JsonFormat(pattern = "yyyy-MM-dd-HH.mm.ss")
        LocalDateTime date
) {}
