package com.saga.controller;

import com.saga.dto.TransferRequest;
import com.saga.dto.TransferResponse;
import com.saga.service.ChoreographyService;
import com.saga.service.OrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {
    private final OrchestrationService orchestrationService;
    private final ChoreographyService choreographyService;

    @PostMapping("/orchestration/transfer")
    public TransferResponse orchestrationTransfer(@RequestBody TransferRequest request) {
        return orchestrationService.executeTransfer(request);
    }

    @PostMapping("/choreography/transfer")
    public TransferResponse choreographyTransfer(@RequestBody TransferRequest request) {
        return choreographyService.initiateTransfer(request);
    }

}
