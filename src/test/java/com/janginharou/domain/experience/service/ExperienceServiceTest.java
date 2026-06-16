package com.janginharou.domain.experience.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.experience.dto.ExperienceRequest;
import com.janginharou.domain.experience.dto.ExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceDifficulty;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.experience.repository.ExperienceScheduleRepository;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.global.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private ExperienceScheduleRepository experienceScheduleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ArtisanRepository artisanRepository;

    private ExperienceService experienceService;
    private Artisan artisan;

    @BeforeEach
    void setUp() {
        experienceService = new ExperienceService(
                experienceRepository,
                experienceScheduleRepository,
                reservationRepository,
                artisanRepository
        );

        User user = User.builder()
                .id(1L)
                .email("artisan@example.com")
                .provider("supabase")
                .nickname("artisan")
                .build();
        artisan = Artisan.builder()
                .id(10L)
                .user(user)
                .name("장인")
                .heritageCategory("공예")
                .isVerified(true)
                .isActive(true)
                .build();
    }

    @Test
    void createsExperienceWithSchedulesForVerifiedArtisan() {
        LocalDateTime firstSlot = LocalDateTime.now().plusDays(1);
        LocalDateTime secondSlot = LocalDateTime.now().plusDays(2);
        ExperienceRequest request = ExperienceRequest.builder()
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER)
                .supportedLanguages(Set.of("ko", "en"))
                .locationAddress("서울 종로구")
                .tags(List.of("매듭", "전통"))
                .schedules(List.of(
                        ExperienceRequest.ScheduleRequest.builder()
                                .scheduledAt(firstSlot)
                                .availableSlots(5)
                                .build(),
                        ExperienceRequest.ScheduleRequest.builder()
                                .scheduledAt(secondSlot)
                                .availableSlots(3)
                                .build()
                ))
                .build();

        when(artisanRepository.findById(artisan.getId())).thenReturn(Optional.of(artisan));
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> {
            Experience experience = invocation.getArgument(0);
            return Experience.builder()
                    .id(100L)
                    .artisan(experience.getArtisan())
                    .title(experience.getTitle())
                    .description(experience.getDescription())
                    .culturalStory(experience.getCulturalStory())
                    .category(experience.getCategory())
                    .price(experience.getPrice())
                    .durationMinutes(experience.getDurationMinutes())
                    .maxParticipants(experience.getMaxParticipants())
                    .difficulty(experience.getDifficulty())
                    .supportedLanguages(experience.getSupportedLanguages())
                    .locationAddress(experience.getLocationAddress())
                    .tags(experience.getTags())
                    .isActive(experience.getIsActive())
                    .build();
        });
        when(experienceScheduleRepository.save(any(ExperienceSchedule.class))).thenAnswer(invocation -> {
            ExperienceSchedule schedule = invocation.getArgument(0);
            return ExperienceSchedule.builder()
                    .id(schedule.getScheduledAt().equals(firstSlot) ? 1L : 2L)
                    .experience(schedule.getExperience())
                    .scheduledAt(schedule.getScheduledAt())
                    .availableSlots(schedule.getAvailableSlots())
                    .isActive(schedule.getIsActive())
                    .build();
        });
        when(reservationRepository.sumParticipantsByScheduleIdAndStatusIn(any(), anyList())).thenReturn(0);

        ExperienceResponse response = experienceService.createExperience(artisan.getId(), request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getArtisanId()).isEqualTo(artisan.getId());
        assertThat(response.getSchedules()).hasSize(2);
        assertThat(response.getSchedules()).extracting("remainingSlots").containsExactly(5, 3);

        ArgumentCaptor<Experience> experienceCaptor = ArgumentCaptor.forClass(Experience.class);
        verify(experienceRepository).save(experienceCaptor.capture());
        assertThat(experienceCaptor.getValue().getTags()).containsExactly("공예", "매듭", "전통");
        verify(experienceScheduleRepository, times(2)).save(any(ExperienceSchedule.class));
    }

    @Test
    void rejectsUnverifiedArtisan() {
        Artisan pendingArtisan = Artisan.builder()
                .id(11L)
                .user(artisan.getUser())
                .name("미승인 장인")
                .heritageCategory("공예")
                .isVerified(false)
                .build();
        ExperienceRequest request = ExperienceRequest.builder()
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER)
                .startDateTime(LocalDateTime.now().plusDays(1))
                .build();

        when(artisanRepository.findById(pendingArtisan.getId())).thenReturn(Optional.of(pendingArtisan));

        assertThatThrownBy(() -> experienceService.createExperience(pendingArtisan.getId(), request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Only verified artisans");
    }

    @Test
    void createsSingleScheduleFromLegacyStartDateTime() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        ExperienceRequest request = ExperienceRequest.builder()
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .price(BigDecimal.valueOf(30000))
                .startDateTime(start)
                .endDateTime(start.plusHours(2))
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER)
                .build();

        when(artisanRepository.findById(artisan.getId())).thenReturn(Optional.of(artisan));
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(experienceScheduleRepository.save(any(ExperienceSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.sumParticipantsByScheduleIdAndStatusIn(eq(null), anyList())).thenReturn(0);

        ExperienceResponse response = experienceService.createExperience(artisan.getId(), request);

        assertThat(response.getDurationMinutes()).isEqualTo(120);
        assertThat(response.getSchedules()).hasSize(1);
        assertThat(response.getSchedules().get(0).getScheduledAt()).isEqualTo(start);
        assertThat(response.getSchedules().get(0).getAvailableSlots()).isEqualTo(8);
    }
}
