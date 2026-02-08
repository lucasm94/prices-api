package com.inditex.prices.infrastructure.adapter.out.persistence;

import com.inditex.prices.domain.exception.NotFoundException;
import com.inditex.prices.domain.exception.ServiceUnavailableException;
import com.inditex.prices.infrastructure.adapter.out.persistence.entity.PriceEntity;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceRepositoryAdapterTest {

    @Mock
    private JpaPriceRepository jpaPriceRepository;

    @Mock
    private MetricsRecorder metrics;

    @InjectMocks
    private PriceRepositoryAdapter adapter;

    @Test
    void shouldReturnMappedPriceAndRecordDatabaseFetch_WhenEntityExists() {
        // Given
        var date = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        var entity = PriceEntity.builder()
                .id(10L)
                .brandId(1L)
                .startDate(date.minusHours(1))
                .endDate(date.plusHours(1))
                .priceList(2)
                .productId(35455L)
                .priority(5)
                .price(new BigDecimal("99.99"))
                .currency("EUR")
                .build();

        when(jpaPriceRepository.findTopPrice(date, 35455L, 1L)).thenReturn(Optional.of(entity));

        // When
        var actual = adapter.getPrice(date, 35455L, 1L);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.brandId()).isEqualTo(entity.getBrandId());
        assertThat(actual.startDate()).isEqualTo(entity.getStartDate());
        assertThat(actual.endDate()).isEqualTo(entity.getEndDate());
        assertThat(actual.priceList()).isEqualTo(entity.getPriceList());
        assertThat(actual.productId()).isEqualTo(entity.getProductId());
        assertThat(actual.priority()).isEqualTo(entity.getPriority());
        assertThat(actual.price()).isEqualTo(entity.getPrice());
        assertThat(actual.currency()).isEqualTo(entity.getCurrency());

        verify(jpaPriceRepository, times(1)).findTopPrice(date, 35455L, 1L);
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.DATABASE_FETCH));
        verifyNoMoreInteractions(metrics);
    }

    @Test
    void shouldThrowNotFoundExceptionAndRecordDatabaseFetch_WhenEntityMissing() {
        // Given
        var date = LocalDateTime.now();
        when(jpaPriceRepository.findTopPrice(date, 1L, 1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> adapter.getPrice(date, 1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Price not found for product");

        verify(jpaPriceRepository, times(1)).findTopPrice(date, 1L, 1L);
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.DATABASE_FETCH));
    }

    @Test
    void shouldMapEntityWithNullFields_ToDomainCorrectly() {
        // Given
        var date = LocalDateTime.of(2021, 1, 1, 0, 0);
        var entity = PriceEntity.builder()
                .id(20L)
                .brandId(null)
                .startDate(null)
                .endDate(null)
                .priceList(null)
                .productId(123L)
                .priority(null)
                .price(null)
                .currency(null)
                .build();

        when(jpaPriceRepository.findTopPrice(date, 123L, null)).thenReturn(Optional.of(entity));

        // When
        var actual = adapter.getPrice(date, 123L, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.brandId()).isNull();
        assertThat(actual.startDate()).isNull();
        assertThat(actual.endDate()).isNull();
        assertThat(actual.priceList()).isNull();
        assertThat(actual.productId()).isEqualTo(entity.getProductId());
        assertThat(actual.priority()).isNull();
        assertThat(actual.price()).isNull();
        assertThat(actual.currency()).isNull();

        verify(jpaPriceRepository, times(1)).findTopPrice(date, 123L, null);
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.DATABASE_FETCH));
    }

    @Test
    void shouldRecordCacheInvalidationMetric_WhenInvalidatePriceCalled() {
        // Given
        var date = LocalDateTime.now();
        var productId = 1L;
        var brandId = 1L;

        // When
        adapter.invalidatePrice(date, productId, brandId);

        // Then: the adapter should call the metrics recorder for cache invalidation and not touch the JPA provider
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.CACHE_INVALIDATION));
        verifyNoInteractions(jpaPriceRepository);
        verifyNoMoreInteractions(metrics);
    }

    @Test
    void shouldPassNullBrandIdAndRecordDatabaseFetch_WhenBrandIdIsNull() {
        // Given
        var date = LocalDateTime.of(2022, 3, 3, 12, 0);
        var entity = PriceEntity.builder()
                .id(30L)
                .brandId(null)
                .startDate(date.minusDays(1))
                .endDate(date.plusDays(1))
                .priceList(1)
                .productId(555L)
                .priority(0)
                .price(new BigDecimal("10.00"))
                .currency("USD")
                .build();

        when(jpaPriceRepository.findTopPrice(date, 555L, null)).thenReturn(Optional.of(entity));

        // When
        var actual = adapter.getPrice(date, 555L, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.brandId()).isNull();
        assertThat(actual.productId()).isEqualTo(555L);

        verify(jpaPriceRepository, times(1)).findTopPrice(date, 555L, null);
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.DATABASE_FETCH));
    }

    @Test
    void shouldHandleNegativeProductIdAndRecordDatabaseFetch_WhenProductIdIsNegative() {
        // Given
        var date = LocalDateTime.of(2022, 5, 5, 8, 30);
        var negativeProductId = -10L;
        var entity = PriceEntity.builder()
                .id(40L)
                .brandId(2L)
                .startDate(date.minusHours(2))
                .endDate(date.plusHours(2))
                .priceList(3)
                .productId(negativeProductId)
                .priority(1)
                .price(new BigDecimal("5.00"))
                .currency("GBP")
                .build();

        when(jpaPriceRepository.findTopPrice(date, negativeProductId, 2L)).thenReturn(Optional.of(entity));

        // When
        var actual = adapter.getPrice(date, negativeProductId, 2L);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.productId()).isEqualTo(negativeProductId);

        verify(jpaPriceRepository, times(1)).findTopPrice(date, negativeProductId, 2L);
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.DATABASE_FETCH));
    }

    @Test
    void shouldRethrowNotFoundException_WhenHandleGetPriceFailureReceivesNotFound() {
        // Given
        var date = LocalDateTime.now();
        var productId = 1L;
        var brandId = 1L;
        var cause = new NotFoundException("no price");

        // When / Then
        assertThatThrownBy(() -> adapter.handleGetPriceFailure(date, productId, brandId, cause))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no price");

        // metrics must not be called in this path
        verifyNoInteractions(metrics);
    }

    @Test
    void shouldRecordFallbackMetricAndThrowServiceUnavailable_WhenHandleGetPriceFailureReceivesTechnicalError() {
        // Given
        var date = LocalDateTime.now();
        var productId = 2L;
        var brandId = 3L;
        var cause = new RuntimeException("DB down");

        // When / Then
        assertThatThrownBy(() -> adapter.handleGetPriceFailure(date, productId, brandId, cause))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Service unavailable. Please try again later.");

        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.FALLBACK));
    }
}
