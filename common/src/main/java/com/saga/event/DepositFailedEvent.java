package com.saga.event;

public record DepositFailedEvent(
        String sagaId,
        String accountNumber,
        String reason
) {
}
