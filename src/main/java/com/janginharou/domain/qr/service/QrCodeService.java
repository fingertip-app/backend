package com.janginharou.domain.qr.service;

import com.janginharou.domain.qr.dto.QrVerifyResponse;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final ReservationRepository reservationRepository;

    @Value("${app.qr.secret-key:default-secret-key-change-in-production}")
    private String qrSecretKey;

    @Value("${app.timezone:Asia/Seoul}")
    private String appTimezone;

    private static final String QR_TOKEN_PREFIX = "JANGINHAROU-RESERVATION:";

    /**
     * QR 토큰 생성 (멱등성 보장)
     * 같은 reservationId는 항상 같은 토큰 생성
     * 형식: JANGINHAROU-RESERVATION:{UUID 16자}
     */
    public String generateQrToken(Long reservationId) {
        String namespace = "JANGINHAROU-RESERVATION-" + qrSecretKey;
        String input = namespace + "-" + reservationId;
        UUID uuid = UUID.nameUUIDFromBytes(input.getBytes());
        String encoded = uuid.toString().replace("-", "").substring(0, 16);
        return QR_TOKEN_PREFIX + encoded;
    }

    /**
     * 예약 ID로 QR 코드 조회
     */
    @Transactional(readOnly = true)
    public String getQrCodeByReservationId(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getQrCode() == null) {
            throw new InvalidRequestException("QR code not generated yet. Payment must be completed first.");
        }

        return reservation.getQrCode();
    }

    /**
     * QR 토큰 검증 및 예약 정보 반환
     */
    @Transactional
    public QrVerifyResponse verifyQrCode(String qrToken) {
        log.info("Verifying QR code: {}", qrToken);

        Reservation reservation = reservationRepository.findByQrCode(qrToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid QR code"));

        // 1. 체험 시간 검증 (체험 시작 10분 전 ~ 시작 후 20분까지 유효)
        Instant now = Instant.now();
        ZoneId timezone = ZoneId.of(appTimezone);
        Instant scheduledInstant = reservation.getSchedule().getScheduledAt()
                .atZone(timezone)
                .toInstant();
        Instant validFrom = scheduledInstant.minus(10, ChronoUnit.MINUTES);
        Instant validUntil = scheduledInstant.plus(20, ChronoUnit.MINUTES);

        if (now.isBefore(validFrom)) {
            throw new InvalidRequestException("QR code is not yet valid. Please scan 10 minutes before the experience.");
        }

        if (now.isAfter(validUntil)) {
            throw new InvalidRequestException("QR code has expired");
        }

        // 2. 예약 상태 검증 (PAID 이상만 입장 가능)
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            throw new InvalidRequestException("Reservation is pending approval");
        }

        if (reservation.getStatus() == ReservationStatus.APPROVED) {
            throw new InvalidRequestException("Payment not completed. Please complete payment first.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.REJECTED) {
            throw new InvalidRequestException("This reservation has been cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            log.warn("Attempt to scan already completed reservation: {}", reservation.getId());
            throw new InvalidRequestException("This reservation has already been completed");
        }

        // PAID 또는 CONFIRMED → COMPLETED로 자동 전환 (정산 대상)
        if (reservation.getStatus() == ReservationStatus.PAID
                || reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservation.complete();
            log.info("✅ [QR 검증] 예약 완료 처리 - reservationId: {}, status: {} → COMPLETED",
                    reservation.getId(), reservation.getStatus());
        }

        log.info("QR verification successful for reservation: {}", reservation.getId());
        return QrVerifyResponse.from(reservation);
    }

    /**
     * QR 코드 재생성 (결제 완료 후 생성 실패 시 복구용)
     */
    @Transactional
    public String regenerateQrCode(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        // PAID 이상만 재생성 가능
        if (reservation.getStatus() != ReservationStatus.PAID
                && reservation.getStatus() != ReservationStatus.CONFIRMED
                && reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new InvalidRequestException("QR code can only be generated for paid reservations. Current status: " + reservation.getStatus());
        }

        // 기존 QR 덮어쓰기 (재생성)
        String qrToken = generateQrToken(reservationId);
        reservation.setQrCode(qrToken);

        log.info("QR code regenerated for reservation: {}", reservationId);
        return qrToken;
    }
}
