package com.saga.service;

import com.saga.domain.Account;
import com.saga.domain.AccountTransaction;
import com.saga.domain.SagaState;
import com.saga.dto.TransferRequest;
import com.saga.dto.TransferResponse;
import com.saga.event.*;
import com.saga.repository.AccountRepository;
import com.saga.repository.AccountTransactionRepository;
import com.saga.repository.SagaStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChoreographyService {
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final SagaStateRepository sagaStateRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request){
        String sagaId = UUID.randomUUID().toString();

        try{
            Account fromAccount = accountRepository.findByAccountNumber(request.fromAccountNumber())
                    .orElseThrow(() -> new RuntimeException("Source account not found"));

            if(fromAccount.getBalance().compareTo(request.amount()) < 0){
                WithdrawFailedEvent event = new WithdrawFailedEvent(sagaId, request.fromAccountNumber(), "Insufficient balance");
                kafkaTemplate.send("account.withdraw.failed", event);
                return new TransferResponse(sagaId, "FAILED", "Insufficient balance");
            }

            fromAccount.withdraw(request.amount());
            accountRepository.save(fromAccount);

            AccountTransaction withdrawTx = AccountTransaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .accountId(fromAccount.getAccountId())
                    .amount(request.amount())
                    .transactionType("WITHDRAW")
                    .sagaId(sagaId)
                    .status("COMPLETED")
                    .build();

            accountTransactionRepository.save(withdrawTx);

            SagaState sagaState = SagaState.builder()
                    .sagaId(sagaId)
                    .patternType("ORCHESTRATION")
                    .fromAccountId(fromAccount.getAccountId())
                    .toAccountId(request.toAccountNumber())
                    .amount(request.amount())
                    .status("STARTED")
                    .build();

            sagaStateRepository.save(sagaState);

            WithdrawSuccessEvent event = new WithdrawSuccessEvent(sagaId, request.fromAccountNumber(), request.toAccountNumber(), request.amount());
            kafkaTemplate.send("account.withdraw.success",event);

            return new TransferResponse(sagaId, "STARTED", "Transfer initiated");

        } catch (Exception e) {
            WithdrawFailedEvent event = new WithdrawFailedEvent(sagaId, request.fromAccountNumber(), Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
            kafkaTemplate.send("account.withdraw.failed", event);
            return new TransferResponse(sagaId, "FAILED", Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
        }
    }


    @KafkaListener(topics = {"transaction.deposit.failed"}, groupId = "account-service-group")
    public void handleDepositFailed(DepositFailedEvent event){
        compensateWithdraw(event.sagaId());
    }

    @Transactional
    private void compensateWithdraw(String sagaId){
        SagaState sagaState = sagaStateRepository.findById(sagaId).orElse(null);
        if (sagaState == null) {
            return;
        }

        Account fromAccount = accountRepository.findById(sagaState.getFromAccountId()).orElse(null);
        if (fromAccount == null) {
            return;
        }

        fromAccount.deposit(sagaState.getAmount());
        accountRepository.save(fromAccount);

        sagaState.markCompensated();
        sagaStateRepository.save(sagaState);

    }

    @KafkaListener(topics = {"transaction.deposit.success"}, groupId = "account-service-group")
    public void handleDepositSuccess(DepositSuccessEvent event){
        sagaStateRepository.findById(event.sagaId())
                .ifPresent(sagaState -> {
                    sagaState.markCompleted();
                    sagaStateRepository.save(sagaState);
                });
    }


    @KafkaListener(topics = {"notification.failed"}, groupId = "account-service-group")
    public void handleNotificationFailed(NotificationFailedEvent event){
        log.info("[SAGA] Notification failed for saga {}: {}", event.sagaId(), event.reason());

        sagaStateRepository.findById(event.sagaId())
                .ifPresent(sagaState -> {
                    sagaState.markCompletedWithNotificationFailure();
                    sagaStateRepository.save(sagaState);
                });
    }
}
