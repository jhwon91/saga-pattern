package com.saga.dto;

public record NotificationRequest(
        String sagaId,
        String userId,
        String notificationType,
        String message
) {
}
