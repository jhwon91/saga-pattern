package com.saga.event;

import java.math.BigDecimal;

public record WithdrawSuccessEvent(
        String sagaId,
        String accountNumber,
        String toAccountNumber,
        BigDecimal amount
) {
}
