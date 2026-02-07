package com.saga.event;

import java.math.BigDecimal;

public record DepositSuccessEvent(
        String sagaId,
        String accountNumber,
        BigDecimal amount
) {
}
