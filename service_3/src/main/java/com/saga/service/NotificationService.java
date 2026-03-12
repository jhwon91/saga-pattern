package com.saga.service;

import com.saga.domain.Notification;
import com.saga.dto.NotificationRequest;
import com.saga.dto.NotificationResponse;
import com.saga.event.DepositSuccessEvent;
import com.saga.event.NotificationFailedEvent;
import com.saga.event.WithdrawFailedEvent;
import com.saga.repository.NotificationRepository;
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
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request){
        String notificationId = UUID.randomUUID().toString();

        Notification notification = Notification.builder()
                .notificationId(notificationId)
                .userId(request.userId())
                .sagaId(request.sagaId())
                .notificationType(request.notificationType())
                .message(request.message())
                .status("SENT")
                .build();
        notificationRepository.save(notification);

        //TODO -> 실제 알림을 전송 or 이메일 전송
        log.info("[NOTIFICATION] Type: {}, User: {}, Message: {}",request.notificationType(), request.userId(), request.message());
        return new NotificationResponse(notificationId, "SENT");
    }

    @KafkaListener(topics = {"transaction.deposit.success"}, groupId = "notification-service-group")
    @Transactional
    public void handleDepositSuccess(DepositSuccessEvent event){
        try{
            String notificationId = UUID.randomUUID().toString();

            Notification notification = Notification.builder()
                    .notificationId(notificationId)
                    .userId(event.accountNumber())
                    .sagaId(event.sagaId())
                    .notificationType("DEPOSIT_SUCCESS")
                    .message("u received " + event.amount())
                    .status("SENT")
                    .build();
            notificationRepository.save(notification);

            log.info("[NOTIFICATION] Deposit success notification sent to {}",event.accountNumber());
        } catch (Exception e) {
            NotificationFailedEvent notificationProcessingFailed = new NotificationFailedEvent(event.sagaId(), event.accountNumber(), Optional.ofNullable(e.getMessage()).orElse("Notification processing failed"));
            kafkaTemplate.send("notification.failed", notificationProcessingFailed);
            log.info("[NOTIFICATION] Failed to send deposit success notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = {"account.withdraw.failed"}, groupId = "notification-service-group")
    @Transactional
    public void handleWithdrawFailed(WithdrawFailedEvent event){
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(event.accountNumber())
                .sagaId(event.sagaId())
                .notificationType("WITHDRAW_FAILED")
                .message("Withdraw failed: "+ event.reason())
                .status("SENT")
                .build();
        notificationRepository.save(notification);
    }
}
