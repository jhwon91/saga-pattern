package com.saga.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "account_id")
    String accountId;

    @Column(name = "account_number", unique = true, nullable = false)
    String accountNumber;

    @Column(nullable = false)
    BigDecimal balance;

    @Column(nullable = false)
    String status = "Active";


    @Column(name = "created_at")
    LocalDateTime createdAt;

    public void withdraw(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}
