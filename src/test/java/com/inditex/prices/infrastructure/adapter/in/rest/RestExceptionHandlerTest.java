package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.domain.exception.NotFoundException;
import com.inditex.prices.domain.exception.ServiceUnavailableException;
import com.inditex.prices.infrastructure.adapter.in.rest.error.ErrorResponse;
import com.inditex.prices.infrastructure.adapter.in.rest.error.RestExceptionHandler;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @Mock
    private MetricsRecorder metrics;

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler(metrics);
    }

    @Test
    void shouldReturnBadRequestAndRecordMetric_WhenNullPointerExceptionThrown() {
        // Given
        var ex = new NullPointerException("some null");

        // When
        ResponseEntity<ErrorResponse> resp = handler.handleGeneralBadRequest(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).contains("some null");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturnBadRequestAndRecordMetric_WhenConstraintViolationExceptionThrown() {
        // Given
        var ex = new ConstraintViolationException("violation", null);

        // When
        var resp = handler.handleGeneralBadRequest(ex);

        // Then
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).contains("violation");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturnBadRequestAndDefaultMessage_WhenIllegalArgumentExceptionHasNullMessage() {
        // Given
        var ex = new IllegalArgumentException((String) null);

        // When
        var resp = handler.handleGeneralBadRequest(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Invalid request data");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturnBadRequestAndRecordMetric_WhenTypeMismatchOccurs() {
        // Given: mock the exception to provide a name
        var ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("date");

        // When
        var resp = handler.handleTypeMismatch(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).contains("Parameter 'date' has an invalid value");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturnBadRequestAndRecordMetric_WhenMissingParamOccurs() {
        // Given: mock the exception
        var ex = mock(MissingServletRequestParameterException.class);
        when(ex.getParameterName()).thenReturn("brandId");

        // When
        var resp = handler.handleMissingParam(ex);

        // Then
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).contains("Required parameter is missing: brandId");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.BAD_REQUEST));
    }

    @Test
    void shouldReturnNotFoundAndRecordMetric_WhenNotFoundException() {
        // Given
        var ex = new NotFoundException("not found");

        // When
        var resp = handler.handleNotFound(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Price not found");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.NOT_FOUND));
    }

    @Test
    void shouldReturnServiceUnavailableAndRecordMetric_WhenServiceUnavailableException() {
        // Given
        var ex = new ServiceUnavailableException("db down");

        // When
        var resp = handler.handleServiceUnavailable(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("db down");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.ERROR));
    }

    @Test
    void shouldReturnInternalServerErrorAndRecordMetric_WhenGenericException() {
        // Given
        var ex = new Exception("boom");

        // When
        var resp = handler.handleGeneric(ex);

        // Then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Internal error");
        verify(metrics, times(1)).recordRequest(eq("price_detail"), eq(MetricsType.ERROR));
    }
}
