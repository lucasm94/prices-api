package com.inditex.prices.infrastructure.monitoring;

import lombok.Getter;

@Getter
public enum MetricsType {
    SUCCESS("success"),
    ERROR("error"),
    BAD_REQUEST("bad_request"),
    NOT_FOUND("not_found"),
    DATABASE_FETCH("database_fetch"),
    CACHE_INVALIDATION("cache_invalidation"),
    FALLBACK("fallback");

    private final String value;

    MetricsType(String value) {
        this.value = value;
    }

}
