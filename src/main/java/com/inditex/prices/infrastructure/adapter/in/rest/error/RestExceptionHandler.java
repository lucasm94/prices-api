package com.inditex.prices.infrastructure.adapter.in.rest.error;

import com.inditex.prices.domain.exception.NotFoundException;
import com.inditex.prices.domain.exception.ServiceUnavailableException;
import com.inditex.prices.infrastructure.monitoring.MetricsEndpoint;
import com.inditex.prices.infrastructure.monitoring.MetricsRecorder;
import com.inditex.prices.infrastructure.monitoring.MetricsType;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.inditex.prices.infrastructure.adapter.in.rest.error.ErrorMessage.INTERNAL_ERROR;
import static com.inditex.prices.infrastructure.adapter.in.rest.error.ErrorMessage.PRICE_NOT_FOUND;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestExceptionHandler {
    private final MetricsRecorder metrics;

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleGeneralBadRequest(Exception ex) {
        log.warn("Validation error: {}", ex.getMessage());
        String message = (ex.getMessage() != null) ? ex.getMessage() : "Invalid request data";
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.BAD_REQUEST);
        return buildResponse(BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' has an invalid value", ex.getName());
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.BAD_REQUEST);
        return buildResponse(BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter is missing: %s", ex.getParameterName());
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.BAD_REQUEST);
        return buildResponse(BAD_REQUEST, message);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.info("Not found exception: {}", ex.getMessage());
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.NOT_FOUND);
        return buildResponse(NOT_FOUND, PRICE_NOT_FOUND.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex) {
        log.error("Unavailable service: {}", ex.getMessage());
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.ERROR);
        return buildResponse(SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled error", ex);
        metrics.recordRequest(MetricsEndpoint.PRICE_DETAIL.getValue(), MetricsType.ERROR);
        return buildResponse(INTERNAL_SERVER_ERROR, INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build(), status);
    }
}
