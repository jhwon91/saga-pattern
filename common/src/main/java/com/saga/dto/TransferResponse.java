package com.saga.dto;

public record TransferResponse(
        String sagaId,
        String status,
        String message
) {
}
