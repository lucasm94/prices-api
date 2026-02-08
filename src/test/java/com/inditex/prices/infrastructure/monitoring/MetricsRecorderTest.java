package com.inditex.prices.infrastructure.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MetricsRecorderTest {

    @Test
    void shouldRegisterCounterAndIncrement_WhenRecordRequestCalled() {
        // Given
        var registry = new SimpleMeterRegistry();
        var recorder = new MetricsRecorder(registry);
        var endpoint = "MY_ENDPOINT";

        // When
        recorder.recordRequest(endpoint, MetricsType.SUCCESS);

        // Then
        var counter = registry.get("api.requests").tag("flow", endpoint).tag("type", MetricsType.SUCCESS.getValue()).counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}

