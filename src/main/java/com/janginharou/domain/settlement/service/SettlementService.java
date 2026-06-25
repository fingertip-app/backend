package com.janginharou.domain.settlement.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.settlement.entity.Settlement;
import com.janginharou.domain.settlement.entity.SettlementItem;
import com.janginharou.domain.settlement.entity.SettlementStatus;
import com.janginharou.domain.settlement.repository.SettlementItemRepository;
import com.janginharou.domain.settlement.repository.SettlementRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final ReservationRepository reservationRepository;
    private final ArtisanRepository artisanRepository;

    private static final BigDecimal DEFAULT_PLATFORM_FEE_RATE = BigDecimal.valueOf(0.10); // 10%

    /**
     * 정산 대상 예약 조회 (COMPLETED 상태이고 아직 정산되지 않은 예약)
     */
    @Transactional(readOnly = true)
    public List<Reservation> getUnsettledReservations(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return reservationRepository.findUnsettledReservations(
                ReservationStatus.COMPLETED,
                startDateTime,
                endDateTime
        );
    }

    /**
     * 장인별 정산 생성 (주간/월간 배치)
     */
    @Transactional
    public List<Settlement> createSettlements(LocalDate startDate, LocalDate endDate) {
        log.info("🔔 [정산 생성] 시작 - 기간: {} ~ {}", startDate, endDate);

        List<Reservation> unsettledReservations = getUnsettledReservations(startDate, endDate);
        log.info("✅ [정산 생성] 정산 대상 예약: {}건", unsettledReservations.size());

        if (unsettledReservations.isEmpty()) {
            log.warn("⚠️ [정산 생성] 정산 대상 예약이 없습니다.");
            return List.of();
        }

        // 장인별로 예약 그룹핑
        Map<Long, List<Reservation>> reservationsByArtisan = unsettledReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getExperience().getArtisan().getId()));

        log.info("✅ [정산 생성] 대상 장인: {}명", reservationsByArtisan.size());

        return reservationsByArtisan.entrySet().stream()
                .map(entry -> createSettlementForArtisan(entry.getKey(), entry.getValue(), startDate, endDate))
                .collect(Collectors.toList());
    }

    /**
     * 특정 장인의 정산 생성
     */
    private Settlement createSettlementForArtisan(
            Long artisanId,
            List<Reservation> reservations,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // 이미 로드된 Artisan 재사용 (N+1 방지)
        Artisan artisan = reservations.get(0).getExperience().getArtisan();

        // 총 매출 계산
        BigDecimal totalRevenue = reservations.stream()
                .map(Reservation::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 플랫폼 수수료 계산
        BigDecimal platformFee = totalRevenue.multiply(DEFAULT_PLATFORM_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // 정산 금액 계산
        BigDecimal settlementAmount = totalRevenue.subtract(platformFee);

        // Settlement 생성
        Settlement settlement = Settlement.builder()
                .artisan(artisan)
                .settlementStartDate(startDate)
                .settlementEndDate(endDate)
                .totalRevenue(totalRevenue)
                .platformFeeRate(DEFAULT_PLATFORM_FEE_RATE)
                .platformFee(platformFee)
                .settlementAmount(settlementAmount)
                .bankAccount("Mock Bank Account") // Mock용
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);
        log.info("✅ [정산 생성] settlementId: {}, artisan: {}, amount: {}",
                savedSettlement.getId(), artisan.getName(), settlementAmount);

        // SettlementItem 일괄 생성 (N+1 방지)
        List<SettlementItem> items = reservations.stream()
                .map(reservation -> createSettlementItem(savedSettlement, reservation))
                .collect(Collectors.toList());
        settlementItemRepository.saveAll(items);

        return savedSettlement;
    }

    /**
     * 정산 항목 생성 (저장하지 않고 엔티티만 생성)
     */
    private SettlementItem createSettlementItem(Settlement settlement, Reservation reservation) {
        BigDecimal amount = reservation.getTotalPrice();
        BigDecimal feeAmount = amount.multiply(settlement.getPlatformFeeRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal settlementAmount = amount.subtract(feeAmount);

        return SettlementItem.builder()
                .settlement(settlement)
                .reservation(reservation)
                .amount(amount)
                .feeAmount(feeAmount)
                .settlementAmount(settlementAmount)
                .build();
    }

    /**
     * 정산 완료 처리 (송금 완료)
     */
    @Transactional
    public Settlement completeSettlement(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", "id", settlementId));

        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new InvalidRequestException("Settlement is not in PENDING status");
        }

        String mockTransferReference = "TRANSFER-" + UUID.randomUUID();
        settlement.complete(mockTransferReference);

        log.info("✅ [정산 완료] settlementId: {}, amount: {}", settlementId, settlement.getSettlementAmount());

        return settlement;
    }

    /**
     * 장인별 정산 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Settlement> getSettlementsByArtisan(Long artisanId) {
        return settlementRepository.findByArtisanIdOrderByCreatedAtDesc(artisanId);
    }

    /**
     * 정산 상세 조회
     */
    @Transactional(readOnly = true)
    public Settlement getSettlementById(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", "id", settlementId));
    }

    /**
     * 정산 항목 조회
     */
    @Transactional(readOnly = true)
    public List<SettlementItem> getSettlementItems(Long settlementId) {
        return settlementItemRepository.findBySettlementId(settlementId);
    }

    /**
     * 대기 중인 정산 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Settlement> getPendingSettlements() {
        return settlementRepository.findByStatus(SettlementStatus.PENDING);
    }
}
