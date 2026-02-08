package com.inditex.prices.infrastructure.monitoring;

import lombok.Getter;

@Getter
public enum MetricsEndpoint {
    PRICE_DETAIL("price_detail");

    private final String value;

    MetricsEndpoint(String value) {
        this.value = value;
    }

}