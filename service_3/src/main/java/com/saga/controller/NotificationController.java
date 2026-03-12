package com.saga.controller;

import com.saga.dto.NotificationRequest;
import com.saga.dto.NotificationResponse;
import com.saga.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class NotificationController {
    private NotificationService notificationService;

    @PostMapping("/notification")
    public NotificationResponse sendNotification(@RequestBody NotificationRequest request){
        return notificationService.sendNotification(request);
    }
}
