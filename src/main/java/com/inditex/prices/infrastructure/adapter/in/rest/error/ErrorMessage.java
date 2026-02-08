package com.inditex.prices.infrastructure.adapter.in.rest.error;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    PRICE_NOT_FOUND("Price not found"),
    INTERNAL_ERROR("Internal error");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

}
