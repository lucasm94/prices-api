package com.inditex.prices.infrastructure.adapter.in.rest.dto;

import com.inditex.prices.domain.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PriceResponseTest {

    @Test
    void shouldMapDomainToResponseCorrectly_WhenDomainIsFullyPopulated() {
        // Given
        var date = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        var domain = Price.builder()
                .brandId(1L)
                .startDate(date.minusHours(1))
                .endDate(date.plusHours(1))
                .priceList(3)
                .productId(9999L)
                .priority(7)
                .price(new BigDecimal("123.45"))
                .currency("USD")
                .build();

        // When
        var dto = PriceResponse.fromDomain(domain);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.brandId()).isEqualTo(domain.brandId());
        assertThat(dto.startDate()).isEqualTo(domain.startDate());
        assertThat(dto.endDate()).isEqualTo(domain.endDate());
        assertThat(dto.priceList()).isEqualTo(domain.priceList());
        assertThat(dto.productId()).isEqualTo(domain.productId());
        assertThat(dto.priority()).isEqualTo(domain.priority());
        assertThat(dto.price()).isEqualTo(domain.price());
        assertThat(dto.curr()).isEqualTo(domain.currency());
    }

    @Test
    void shouldReturnNull_WhenDomainIsNull() {
        // Given
        Price domain = null;

        // When
        var dto = PriceResponse.fromDomain(domain);

        // Then
        assertThat(dto).isNull();
    }
}

