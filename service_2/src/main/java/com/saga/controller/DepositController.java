package com.saga.controller;

import com.saga.dto.DepositRequest;
import com.saga.dto.DepositResponse;
import com.saga.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class DepositController {
    private final DepositService depositService;

    @PostMapping("/deposit")
    public DepositResponse processDeposit(@RequestBody DepositRequest request){
        return depositService.processDeposit(request);
    }
}
