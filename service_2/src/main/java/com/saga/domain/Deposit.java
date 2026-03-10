package com.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = " deposits")
public class Deposit {
    @Id
    @Column(name = "deposit_id")
    String depositId;

    @Column(name = "transaction_id", nullable = false)
    String transactionId;

    @Column(name = "account_number", nullable = false)
    String accountNumber;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(nullable = false)
    String status;

    @Column(name = "saga_id")
    String sagaId;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Builder
    public Deposit(String depositId, String transactionId, String accountNumber, BigDecimal amount, String status, String sagaId) {
        this.depositId = depositId;
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.status = status;
        this.sagaId = sagaId;
    }
}
