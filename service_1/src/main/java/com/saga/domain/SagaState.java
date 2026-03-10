package com.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "saga_state")
public class SagaState {
    @Id
    @Column(name = "saga_id")
    String sagaId;

    @Column(name = "pattern_type", nullable = false)
    String patternType;

    @Column(name = "from_account_id", nullable = false)
    String fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    String toAccountId;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(nullable = false)
    String status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Builder
    public SagaState(String sagaId, String patternType, String fromAccountId, String toAccountId, BigDecimal amount, String status) {
        this.sagaId = sagaId;
        this.patternType = patternType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
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

    public void markCompletedWithNotificationFailure() {
        this.status = "COMPLETED_WITH_NOTIFICATION_FAILURE";
    }

}
