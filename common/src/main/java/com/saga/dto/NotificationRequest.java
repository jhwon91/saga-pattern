package com.saga.dto;

import java.math.BigDecimal;

public record NotificationRequest(
        String sagaId,
        String userId,
        String notificationType,
        String message
) {
}
