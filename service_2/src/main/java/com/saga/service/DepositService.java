package com.saga.service;

import com.saga.domain.Deposit;
import com.saga.domain.Transaction;
import com.saga.dto.DepositRequest;
import com.saga.dto.DepositResponse;
import com.saga.dto.NotificationRequest;
import com.saga.dto.NotificationResponse;
import com.saga.event.DepositFailedEvent;
import com.saga.event.DepositSuccessEvent;
import com.saga.event.NotificationFailedEvent;
import com.saga.event.WithdrawSuccessEvent;
import com.saga.repository.DepositRepository;
import com.saga.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final TransactionRepository transactionRepository;
    private final DepositRepository depositRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Value("${service.notification.url}")
    private String notification;

    @Transactional
    public DepositResponse processDeposit(DepositRequest request){
        try{
            String transactionId = UUID.randomUUID().toString();
            String depositId = UUID.randomUUID().toString();

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .sagaId(request.sagaId())
                    .fromAccountNumber(request.fromAccountNumber())
                    .toAccountNumber(request.accountNumber())
                    .amount(request.amount())
                    .status("COMPLETED")
                    .build();
            transactionRepository.save(transaction);

            Deposit deposit = Deposit.builder()
                    .depositId(depositId)
                    .transactionId(transactionId)
                    .accountNumber(request.accountNumber())
                    .amount(request.amount())
                    .status("COMPLETED")
                    .sagaId(request.sagaId())
                    .build();
            depositRepository.save(deposit);

            try{
                NotificationRequest notificationRequest = new NotificationRequest(request.sagaId(), request.accountNumber(), "DEPOSIT_SUCCES", "Received " + request.amount() + " from " + request.fromAccountNumber());
                restTemplate.postForObject(notification + "/internal/notification", notificationRequest, NotificationResponse.class);
            } catch (Exception e) {

            }

        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while processing deposit", e);
        }
        return null;
    }

    @KafkaListener(topics = {"account.withdraw.success"}, groupId = "transaction-service-group")
    @Transactional
    public void handleWithdrawSuccess(WithdrawSuccessEvent event){
        try {
            String transactionId = UUID.randomUUID().toString();
            String depositId = UUID.randomUUID().toString();

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .sagaId(event.sagaId())
                    .fromAccountNumber(event.accountNumber())
                    .toAccountNumber(event.toAccountNumber())
                    .amount(event.amount())
                    .status("COMPLETED")
                    .build();
            transactionRepository.save(transaction);

            Deposit deposit = Deposit.builder()
                    .depositId(depositId)
                    .transactionId(transactionId)
                    .accountNumber(event.toAccountNumber())
                    .amount(event.amount())
                    .status("COMPLETED")
                    .sagaId(event.sagaId())
                    .build();
            depositRepository.save(deposit);

            DepositSuccessEvent depositSuccessEvent = new DepositSuccessEvent(event.sagaId(), event.toAccountNumber(), event.amount());
            kafkaTemplate.send("transaction.deposit.success",depositSuccessEvent);

        } catch (Exception e) {
            //DLQ 패턴을 통해 보상 트랜잭션을 따로 구현해도 무방하다.
            DepositFailedEvent depositFailedEvent = new DepositFailedEvent(event.sagaId(), event.toAccountNumber(), Optional.ofNullable(e.getMessage()).orElse("Unknown error"));
            kafkaTemplate.send("transaction.deposit.failed", depositFailedEvent);
        }
    }

    @KafkaListener(topics = {"notification.failed"}, groupId = "transaction-service-group")
    public void handleNotificationFailed(NotificationFailedEvent event){
        log.info("[TRANSACTION] Notification failed for saga {} : {}",event.sagaId(), event.reason());
    }
}
