package com.backend.domain.account.controller;

import com.backend.domain.contract.service.ContractDetailService;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class PerformanceTestController {

    private final ContractDetailService contractDetailService;

    @GetMapping("/performance/{accountId}")
    public ResponseEntity<Map<String, Object>> testPerformance(
            @PathVariable Integer accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {

        Map<String, Object> result = contractDetailService.comparePerformance(accountId, securityUser);

        return ResponseEntity.ok(result);
    }
}