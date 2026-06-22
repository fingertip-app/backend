package com.janginharou.domain.qr.service;

import com.janginharou.domain.qr.dto.QrVerifyResponse;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final ReservationRepository reservationRepository;

    /**
     * QR 토큰 생성
     * 형식: JANGINHAROU-RESERVATION:{UUID 16자}
     */
    public String generateQrToken(Long reservationId) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return "JANGINHAROU-RESERVATION:" + uuid;
    }

    /**
     * 예약 ID로 QR 코드 조회
     */
    @Transactional(readOnly = true)
    public String getQrCodeByReservationId(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getQrCode() == null) {
            throw new InvalidRequestException("QR code not generated yet. Reservation must be CONFIRMED.");
        }

        return reservation.getQrCode();
    }

    /**
     * QR 토큰 검증 및 예약 정보 반환
     */
    @Transactional(readOnly = true)
    public QrVerifyResponse verifyQrCode(String qrToken) {
        log.info("Verifying QR code: {}", qrToken);

        Reservation reservation = reservationRepository.findByQrCode(qrToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid QR code"));

        // 1. 체험 시간 검증 (체험 시작 1시간 후까지 유효)
        LocalDateTime expiresAt = reservation.getSchedule().getScheduledAt()
                .plusHours(1);

        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new InvalidRequestException("QR code has expired");
        }

        // 2. 예약 상태 검증 (CONFIRMED만 입장 가능)
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidRequestException("Reservation is not confirmed. Current status: " + reservation.getStatus());
        }

        log.info("QR verification successful for reservation: {}", reservation.getId());
        return QrVerifyResponse.from(reservation);
    }
}
