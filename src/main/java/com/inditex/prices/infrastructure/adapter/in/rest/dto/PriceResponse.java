package com.inditex.prices.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inditex.prices.domain.model.Price;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PriceResponse(
        Long brandId,
        @JsonFormat(pattern = "yyyy-MM-dd-HH.mm.ss")
        LocalDateTime startDate,
        @JsonFormat(pattern = "yyyy-MM-dd-HH.mm.ss")
        LocalDateTime endDate,
        Integer priceList,
        Long productId,
        Integer priority,
        BigDecimal price,
        @JsonProperty("currency")
        String curr
) {
    /**
     * Maps a Price domain object to a PriceResponse DTO.
     *
     * @param domain the price data from the domain layer
     * @return the mapped PriceResponse for API output, or null if input is null
     */
    public static PriceResponse fromDomain(Price domain) {
        if (domain == null) return null;

        return PriceResponse.builder()
                .brandId(domain.brandId())
                .startDate(domain.startDate())
                .endDate(domain.endDate())
                .priceList(domain.priceList())
                .productId(domain.productId())
                .priority(domain.priority())
                .price(domain.price())
                .curr(domain.currency())
                .build();
    }
}
