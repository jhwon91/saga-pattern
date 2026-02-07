package com.saga.event;

public record WithdrawFailedEvent(
        String sagaId,
        String accountNumber,
        String reason
) {
}
