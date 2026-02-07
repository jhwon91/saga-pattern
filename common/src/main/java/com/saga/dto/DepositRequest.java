package com.saga.dto;

import java.math.BigDecimal;

public record DepositRequest(
        String sagaId,
        String accountNumber,
        BigDecimal amount,
        String fromAccountNumber
) { }

