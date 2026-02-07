package com.saga.event;

public record NotificationFailedEvent(
        String sagaId,
        String accountNumber,
        String reason
) {
}
