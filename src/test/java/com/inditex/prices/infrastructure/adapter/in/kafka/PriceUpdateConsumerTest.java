package com.inditex.prices.infrastructure.adapter.in.kafka;

import com.inditex.prices.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PriceUpdateConsumerTest {

    @Mock
    private PriceRepositoryAdapter priceAdapter;

    @InjectMocks
    private PriceUpdateConsumer consumer;

    @Test
    void shouldInvalidateCache_WhenPriceUpdateMessageReceived() {
        // Given
        var date = LocalDateTime.of(2024, 1, 1, 12, 0);
        var message = PriceUpdateMessage.builder()
                .productId(123L)
                .brandId(1L)
                .date(date)
                .build();

        // When
        consumer.handlePriceUpdate(message);

        // Then
        verify(priceAdapter, times(1)).invalidatePrice(date, 123L, 1L);
        verifyNoMoreInteractions(priceAdapter);
    }

    @Test
    void shouldPropagateRuntimeException_WhenAdapterThrows() {
        // Given
        var date = LocalDateTime.of(2024, 1, 1, 12, 0);
        var message = PriceUpdateMessage.builder()
                .productId(10L)
                .brandId(2L)
                .date(date)
                .build();

        doThrow(new RuntimeException("boom"))
                .when(priceAdapter).invalidatePrice(date, 10L, 2L);

        // When / Then
        assertThatThrownBy(() -> consumer.handlePriceUpdate(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom");

        verify(priceAdapter, times(1)).invalidatePrice(date, 10L, 2L);
    }
}

