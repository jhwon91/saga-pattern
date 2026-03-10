package com.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transactions")
public class AccountTransaction {
    @Id
    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "account_id", nullable = false)
    String accountId;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    String transactionType;

    @Column(name = "saga_id")
    String sagaId;

    @Column(nullable = false)
    String status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Builder
    public AccountTransaction(String transactionId, String accountId, BigDecimal amount, String transactionType, String sagaId, String status) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.sagaId = sagaId;
        this.status = status;
    }

    public void markCompensated() {
        this.status = "COMPENSATED";
    }

    public void markCompleted() {
        this.status = "COMPLETED";
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}
