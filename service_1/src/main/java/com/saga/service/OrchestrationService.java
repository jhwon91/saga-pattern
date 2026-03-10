package com.saga.service;

import com.saga.domain.Account;
import com.saga.domain.AccountTransaction;
import com.saga.domain.SagaState;
import com.saga.dto.DepositRequest;
import com.saga.dto.DepositResponse;
import com.saga.dto.TransferRequest;
import com.saga.dto.TransferResponse;
import com.saga.repository.AccountRepository;
import com.saga.repository.AccountTransactionRepository;
import com.saga.repository.SagaStateRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrchestrationService {
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final SagaStateRepository sagaStateRepository;
    private final RestTemplate restTemplate;

    @Value("${service.transaction.url}")
    private final String transactionServiceUrl;

    @Value("${service.notification.url}")
    private final String notificationServiceUrl;

    @Transactional
    public TransferResponse executeTransfer(TransferRequest request){
        String sagaId = UUID.randomUUID().toString();
        try {
            Account fromAccount = accountRepository.findByAccountNumber(request.fromAccountNumber())
                    .orElseThrow(() -> new RuntimeException("Source account not found"));
            if(fromAccount.getBalance().compareTo(request.amount()) < 0) {
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

            try{
                DepositRequest depositRequest = new DepositRequest(sagaId, request.toAccountNumber(), request.amount(), request.fromAccountNumber());

                DepositResponse response = Optional.ofNullable(
                        restTemplate.postForObject(transactionServiceUrl + "/internal/deposit", depositRequest, DepositResponse.class)
                ).orElseThrow(() -> new RuntimeException("Deposit failed"));

                sagaState.markCompleted();
                sagaStateRepository.save(sagaState);

            } catch (Exception e) {
                fromAccount.deposit(request.amount());
                accountRepository.save(fromAccount);

                withdrawTx.markCompensated();
                accountTransactionRepository.save(withdrawTx);

                sagaState.markCompensated();
                sagaStateRepository.save(sagaState);
            }


        } catch (Exception e) {
            return new TransferResponse(sagaId, "FAILED", Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
        }
        return null;
    }

}
