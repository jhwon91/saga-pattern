package com.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "saga_id", nullable = false)
    String sagaId;

    @Column(name = "from_account_number", nullable = false)
    String fromAccountNumber;

    @Column(name = "to_account_number", nullable = false)
    String toAccountNumber;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(nullable = false)
    String status;

    @Column(name = "create_at")
    LocalDateTime createdAt;

    @Builder
    public Transaction(String transactionId, String sagaId, String fromAccountNumber, String toAccountNumber, BigDecimal amount, String status) {
        this.transactionId = transactionId;
        this.sagaId = sagaId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.status = status;
    }
}
