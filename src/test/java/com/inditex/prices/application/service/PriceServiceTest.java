package com.inditex.prices.application.service;

import com.inditex.prices.domain.exception.NotFoundException;
import com.inditex.prices.domain.exception.ServiceUnavailableException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.repository.PriceRepository;
import com.inditex.prices.infrastructure.monitoring.MetricsEndpoint;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private MetricsRecorder metrics;

    @InjectMocks
    private PriceService priceService;

    @Test
    void shouldReturnPriceAndRecordSuccess_WhenPriceExists() {
        // Given
        var date = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        var expected = Price.builder()
                .brandId(1L)
                .startDate(date.minusHours(1))
                .endDate(date.plusHours(1))
                .priceList(1)
                .productId(35455L)
                .priority(1)
                .price(new BigDecimal("35.50"))
                .currency("EUR")
                .build();

        when(priceRepository.getPrice(date, 35455L, 1L)).thenReturn(expected);

        // When
        var actual = priceService.getPrice(date, 35455L, 1L);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(metrics, times(1)).recordRequest(eq(MetricsEndpoint.PRICE_DETAIL.getValue()), eq(MetricsType.SUCCESS));
        verifyNoMoreInteractions(metrics);
    }

    @Test
    void shouldThrowNullPointerException_WhenDateIsNull() {
        // Given
        LocalDateTime date = null;

        // When / Then
        assertThatThrownBy(() -> priceService.getPrice(date, 1L, 1L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("The date must not be null");

        verifyNoInteractions(priceRepository);
        verifyNoInteractions(metrics);
    }

    @Test
    void shouldPropagateNotFoundException_WhenRepositoryThrowsNotFound() {
        // Given
        var date = LocalDateTime.now();
        when(priceRepository.getPrice(date, 1L, 1L)).thenThrow(new NotFoundException("not found"));

        // When / Then
        assertThatThrownBy(() -> priceService.getPrice(date, 1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");

        verify(priceRepository, times(1)).getPrice(date, 1L, 1L);
        verifyNoInteractions(metrics);
    }

    @Test
    void shouldPropagateServiceUnavailableException_WhenRepositoryThrowsServiceUnavailable() {
        // Given
        var date = LocalDateTime.now();
        when(priceRepository.getPrice(date, 1L, 1L)).thenThrow(new ServiceUnavailableException("db down"));

        // When / Then
        assertThatThrownBy(() -> priceService.getPrice(date, 1L, 1L))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("db down");

        verify(priceRepository, times(1)).getPrice(date, 1L, 1L);
        verifyNoInteractions(metrics);
    }
}

