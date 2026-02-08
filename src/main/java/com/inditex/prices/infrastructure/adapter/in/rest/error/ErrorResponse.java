package com.inditex.prices.infrastructure.adapter.in.rest.error;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
}
