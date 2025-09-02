package com.backend.domain.account.controller;


import com.backend.domain.contract.service.ContractDetailService;
import com.backend.domain.transaction.dto.*;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
//
//    @Autowired
//    private ContractDetailService contractService;
//
//    @GetMapping("/test/{accountId}")
//    public Map<String, Object> performanceTest(@PathVariable Integer accountId,
//                                               @AuthenticationPrincipal SecurityUser securityUser) {
//
//        System.out.println("🚀 성능 테스트 시작 - 계좌 ID: " + accountId);
//
//        // V1 성능 측정 (N+1 문제) - 1000회
//        long v1Total = measureTotal(() ->
//                contractService.getContractDetailV1(accountId, securityUser), 1000);
//
//        // V2 성능 측정 (JOIN FETCH) - 1000회
//        long v2Total = measureTotal(() ->
//                contractService.getContractDetailV2(accountId, securityUser), 1000);
//
//        // 결과 출력
//        long v1Avg = v1Total / 1000;
//        long v2Avg = v2Total / 1000;
//
//        double improvement = ((double)(v1Avg - v2Avg) / v1Avg) * 100;
//        double speedup = (double)v1Avg / v2Avg;
//
//        // 콘솔 출력
//        System.out.println("📊 ===== 성능 측정 결과 (1000회) =====");
//        System.out.println("🐌 V1 총 실행시간: " + v1Total + "ms (평균: " + v1Avg + "ms)");
//        System.out.println("⚡ V2 총 실행시간: " + v2Total + "ms (평균: " + v2Avg + "ms)");
//        System.out.println("🚀 성능 개선: " + String.format("%.1f", improvement) + "%");
//        System.out.println("⏱️ 속도 향상: " + String.format("%.1f", speedup) + "배");
//        System.out.println("📈 총 시간 절약: " + (v1Total - v2Total) + "ms");
//
//        // JSON 응답
//        Map<String, Object> result = new HashMap<>();
//        result.put("accountId", accountId);
//        result.put("iterations", 1000);
//        result.put("v1_total_ms", v1Total);
//        result.put("v2_total_ms", v2Total);
//        result.put("v1_average_ms", v1Avg);
//        result.put("v2_average_ms", v2Avg);
//        result.put("improvement_percent", Math.round(improvement * 10) / 10.0);
//        result.put("speedup_times", Math.round(speedup * 10) / 10.0);
//        result.put("total_time_saved_ms", v1Total - v2Total);
//
//        return result;
//    }
//
//    @GetMapping("/quick-test/{accountId}")
//    public Map<String, Object> quickTest(@PathVariable Integer accountId,
//                                         @AuthenticationPrincipal SecurityUser securityUser) {
//
//        // 각각 1번씩만 실행해서 빠르게 테스트
//        long v1Time = measureOnce(() -> contractService.getContractDetailV1(accountId, securityUser));
//        long v2Time = measureOnce(() -> contractService.getContractDetailV2(accountId, securityUser));
//
//        System.out.println("⚡ 빠른 테스트 결과:");
//        System.out.println("🐌 V1: " + v1Time + "ms");
//        System.out.println("⚡ V2: " + v2Time + "ms");
//
//        return Map.of(
//                "v1_ms", v1Time,
//                "v2_ms", v2Time,
//                "difference_ms", v1Time - v2Time
//        );
//    }

    private long measureTotal(Runnable operation, int iterations) {
        // 워밍업 (JVM 최적화)
        System.out.println("🔥 워밍업 중...");
        for (int i = 0; i < 10; i++) {
            operation.run();
        }

        // 실제 측정
        System.out.println("⏱️ 측정 시작... (" + iterations + "회)");
        long totalStartTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            operation.run();

            // 100회마다 진행상황 출력
            if ((i + 1) % 100 == 0) {
                System.out.println("진행: " + (i + 1) + "/" + iterations + " 완료");
            }
        }

        long totalEndTime = System.nanoTime();
        long totalTimeMs = (totalEndTime - totalStartTime) / 1_000_000;

        System.out.println("✅ 측정 완료! 총 " + totalTimeMs + "ms");
        return totalTimeMs;
    }

    private long measureOnce(Runnable operation) {
        long start = System.nanoTime();
        operation.run();
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }
}