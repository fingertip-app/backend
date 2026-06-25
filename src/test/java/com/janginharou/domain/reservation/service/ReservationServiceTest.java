package com.janginharou.domain.reservation.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.experience.repository.ExperienceScheduleRepository;
import com.janginharou.domain.reservation.dto.ReservationRequest;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.event.ReservationStatusChangedEvent;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.review.repository.ReviewRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private ExperienceScheduleRepository experienceScheduleRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private com.janginharou.domain.qr.service.QrCodeService qrCodeService;

    private ReservationService reservationService;

    private User user;
    private Artisan artisan;
    private Experience experience;
    private ExperienceSchedule schedule;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                userRepository,
                experienceRepository,
                experienceScheduleRepository,
                reviewRepository,
                eventPublisher,
                qrCodeService
        );

        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .provider("local")
                .nickname("user")
                .build();
        artisan = Artisan.builder()
                .id(10L)
                .user(user)
                .name("장인")
                .heritageCategory("craft")
                .build();
        experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("체험")
                .description("설명")
                .category("craft")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .isActive(true)
                .build();
        schedule = ExperienceSchedule.builder()
                .id(1000L)
                .experience(experience)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .availableSlots(5)
                .isActive(true)
                .build();
    }

    @Test
    void createsPendingReservationByScheduleCapacity() {
        ReservationRequest request = ReservationRequest.builder()
                .experienceId(experience.getId())
                .scheduleId(schedule.getId())
                .numberOfParticipants(2)
                .requestMessage("잘 부탁드립니다")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(experienceScheduleRepository.findByIdForUpdate(schedule.getId())).thenReturn(Optional.of(schedule));
        when(reservationRepository.existsByUserIdAndScheduleIdAndStatusIn(eq(user.getId()), eq(schedule.getId()), any()))
                .thenReturn(false);
        when(reservationRepository.sumParticipantsByScheduleIdAndStatusIn(eq(schedule.getId()), any()))
                .thenReturn(3);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation reservation = reservationService.createReservation(user.getId(), request);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getSchedule()).isEqualTo(schedule);
        assertThat(reservation.getReservedDateTime()).isEqualTo(schedule.getScheduledAt());
        assertThat(reservation.getTotalPrice()).isEqualByComparingTo("60000");
    }

    @Test
    void rejectsDuplicateActiveReservationForSameSchedule() {
        ReservationRequest request = ReservationRequest.builder()
                .experienceId(experience.getId())
                .scheduleId(schedule.getId())
                .numberOfParticipants(1)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(experienceScheduleRepository.findByIdForUpdate(schedule.getId())).thenReturn(Optional.of(schedule));
        when(reservationRepository.existsByUserIdAndScheduleIdAndStatusIn(eq(user.getId()), eq(schedule.getId()), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(user.getId(), request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Duplicate active reservation");
    }

    @Test
    void rejectsWhenScheduleCapacityIsExceeded() {
        ReservationRequest request = ReservationRequest.builder()
                .experienceId(experience.getId())
                .scheduleId(schedule.getId())
                .numberOfParticipants(3)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(experienceScheduleRepository.findByIdForUpdate(schedule.getId())).thenReturn(Optional.of(schedule));
        when(reservationRepository.existsByUserIdAndScheduleIdAndStatusIn(eq(user.getId()), eq(schedule.getId()), any()))
                .thenReturn(false);
        when(reservationRepository.sumParticipantsByScheduleIdAndStatusIn(eq(schedule.getId()), any()))
                .thenReturn(4);

        assertThatThrownBy(() -> reservationService.createReservation(user.getId(), request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Booking slot is unavailable");
    }

    @Test
    void transitionsPendingReservationToApprovedAndPaidAndConfirmed() {
        Reservation reservation = pendingReservation();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThat(reservationService.approveReservation(reservation.getId()).getStatus())
                .isEqualTo(ReservationStatus.APPROVED);
        assertThat(reservationService.processPayment(reservation.getId(), reservation.getUser().getId(), "payment-key").getStatus())
                .isEqualTo(ReservationStatus.PAID);
        assertThat(reservationService.confirmReservation(reservation.getId(), reservation.getUser().getId()).getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Reservation reservation = pendingReservation();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.processPayment(reservation.getId(), reservation.getUser().getId(), "payment-key"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid reservation status");
    }

    @Test
    void validatesArtisanOwnershipWhenApproving() {
        Reservation reservation = pendingReservation();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.approveReservation(reservation.getId(), 99L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Reservation does not belong to artisan");
    }

    @Test
    void shouldPublishEventWhenApproveReservation() {
        Reservation reservation = pendingReservation();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        reservationService.approveReservation(reservation.getId());

        verify(eventPublisher).publishEvent(any(ReservationStatusChangedEvent.class));
    }

    @Test
    void shouldPublishEventWhenRejectReservation() {
        Reservation reservation = pendingReservation();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        reservationService.rejectReservation(reservation.getId(), "일정이 맞지 않습니다");

        verify(eventPublisher).publishEvent(any(ReservationStatusChangedEvent.class));
    }

    @Test
    void shouldPublishEventWhenCancelReservation() {
        Reservation reservation = pendingReservation();

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(reservation.getId(), reservation.getUser().getId(), "개인 사정");

        verify(eventPublisher).publishEvent(any(ReservationStatusChangedEvent.class));
    }

    private Reservation pendingReservation() {
        return Reservation.builder()
                .id(200L)
                .user(user)
                .experience(experience)
                .schedule(schedule)
                .numberOfParticipants(1)
                .totalPrice(BigDecimal.valueOf(30000))
                .status(ReservationStatus.PENDING)
                .isNotificationSent(false)
                .build();
    }
}
