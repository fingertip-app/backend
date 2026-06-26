package com.janginharou.domain.settlement.controller;

import com.janginharou.domain.settlement.dto.SettlementItemResponse;
import com.janginharou.domain.settlement.dto.SettlementResponse;
import com.janginharou.domain.settlement.entity.Settlement;
import com.janginharou.domain.settlement.entity.SettlementItem;
import com.janginharou.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 장인별 정산 내역 조회
     */
    @GetMapping("/artisan/{artisanId}")
    public ResponseEntity<Map<String, Object>> getSettlementsByArtisan(@PathVariable Long artisanId) {
        List<Settlement> settlements = settlementService.getSettlementsByArtisan(artisanId);
        List<SettlementResponse> responses = settlements.stream()
                .map(SettlementResponse::from)
                .toList();

        return ResponseEntity.ok(Map.of(
                "data", responses,
                "message", "success"
        ));
    }

    /**
     * 정산 상세 조회
     */
    @GetMapping("/{settlementId}")
    public ResponseEntity<Map<String, Object>> getSettlementDetail(@PathVariable Long settlementId) {
        Settlement settlement = settlementService.getSettlementById(settlementId);
        List<SettlementItem> items = settlementService.getSettlementItems(settlementId);

        SettlementResponse settlementResponse = SettlementResponse.from(settlement);
        List<SettlementItemResponse> itemResponses = items.stream()
                .map(SettlementItemResponse::from)
                .toList();

        return ResponseEntity.ok(Map.of(
                "data", Map.of(
                        "settlement", settlementResponse,
                        "items", itemResponses
                ),
                "message", "success"
        ));
    }

    /**
     * 대기 중인 정산 목록 조회 (관리자용)
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingSettlements() {
        List<Settlement> settlements = settlementService.getPendingSettlements();
        List<SettlementResponse> responses = settlements.stream()
                .map(SettlementResponse::from)
                .toList();

        return ResponseEntity.ok(Map.of(
                "data", responses,
                "message", "success"
        ));
    }

    /**
     * 정산 생성 (관리자용 배치)
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createSettlementBatch(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<Settlement> settlements = settlementService.createSettlements(startDate, endDate);
        List<SettlementResponse> responses = settlements.stream()
                .map(SettlementResponse::from)
                .toList();

        return ResponseEntity.ok(Map.of(
                "data", responses,
                "message", String.format("정산 생성 완료: %d건", settlements.size())
        ));
    }

    /**
     * 정산 완료 처리 (관리자용)
     */
    @PostMapping("/{settlementId}/complete")
    public ResponseEntity<Map<String, Object>> completeSettlement(@PathVariable Long settlementId) {
        Settlement settlement = settlementService.completeSettlement(settlementId);
        SettlementResponse response = SettlementResponse.from(settlement);

        return ResponseEntity.ok(Map.of(
                "data", response,
                "message", "정산 완료"
        ));
    }
}
