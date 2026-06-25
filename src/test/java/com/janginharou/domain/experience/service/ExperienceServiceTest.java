package com.janginharou.domain.experience.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.experience.dto.AddExperienceImagesRequest;
import com.janginharou.domain.experience.dto.ExperienceRequest;
import com.janginharou.domain.experience.dto.ExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceImage;
import com.janginharou.domain.experience.entity.ExperienceDifficulty;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceImageRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
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

    @Mock
    private ExperienceImageRepository experienceImageRepository;

    @Mock
    private com.janginharou.domain.review.repository.ReviewRepository reviewRepository;

    @Mock
    private com.janginharou.domain.wishlist.repository.WishlistRepository wishlistRepository;

    @Mock
    private com.janginharou.domain.cardnews.repository.CardNewsExperienceRepository cardNewsExperienceRepository;

    private ExperienceService experienceService;
    private Artisan artisan;

    @BeforeEach
    void setUp() {
        experienceService = new ExperienceService(
                experienceRepository,
                experienceScheduleRepository,
                reservationRepository,
                artisanRepository,
                experienceImageRepository,
                reviewRepository,
                wishlistRepository,
                cardNewsExperienceRepository
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
                .imageUrl("https://example.com/main.jpg")
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
        assertThat(response.getImages()).hasSize(1);
        assertThat(response.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/main.jpg");

        ArgumentCaptor<Experience> experienceCaptor = ArgumentCaptor.forClass(Experience.class);
        verify(experienceRepository).save(experienceCaptor.capture());
        assertThat(experienceCaptor.getValue().getTags()).containsExactly("공예", "매듭", "전통");
        verify(experienceScheduleRepository, times(2)).save(any(ExperienceSchedule.class));
        verify(experienceImageRepository).save(any(ExperienceImage.class));
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

    @Test
    void returnsSchedulesInActiveExperienceList() {
        Experience experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER.name())
                .supportedLanguages(List.of("ko", "en"))
                .locationAddress("서울 종로구")
                .isActive(true)
                .build();
        ExperienceSchedule schedule = ExperienceSchedule.builder()
                .id(300L)
                .experience(experience)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .availableSlots(5)
                .isActive(true)
                .build();

        when(experienceRepository.findByIsActiveTrue()).thenReturn(List.of(experience));
        when(experienceScheduleRepository.findByExperienceIdAndIsActiveTrue(experience.getId()))
                .thenReturn(List.of(schedule));
        when(reservationRepository.sumParticipantsByScheduleIdAndStatusIn(eq(schedule.getId()), anyList()))
                .thenReturn(2);

        List<ExperienceResponse> responses = experienceService.getActiveExperienceResponses();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSchedules()).hasSize(1);
        assertThat(responses.get(0).getSchedules().get(0).getId()).isEqualTo(schedule.getId());
        assertThat(responses.get(0).getSchedules().get(0).getRemainingSlots()).isEqualTo(3);
    }

    @Test
    void returnsEmptyImagesWhenExperienceHasNoImages() {
        Experience experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER.name())
                .supportedLanguages(List.of("ko", "en"))
                .locationAddress("서울 종로구")
                .images(null)
                .tags(null)
                .isActive(true)
                .build();

        when(experienceRepository.findByIsActiveTrue()).thenReturn(List.of(experience));
        when(experienceScheduleRepository.findByExperienceIdAndIsActiveTrue(experience.getId()))
                .thenReturn(List.of());

        List<ExperienceResponse> responses = experienceService.getActiveExperienceResponses();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getImages()).isEmpty();
        assertThat(responses.get(0).getTags()).isEmpty();
    }

    @Test
    void sortsImagesByDisplayOrderAscending() {
        ExperienceImage second = ExperienceImage.builder()
                .imageUrl("https://example.com/2.jpg")
                .displayOrder(2)
                .build();
        ExperienceImage first = ExperienceImage.builder()
                .imageUrl("https://example.com/1.jpg")
                .displayOrder(1)
                .build();

        Experience experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER.name())
                .supportedLanguages(List.of("ko", "en"))
                .locationAddress("서울 종로구")
                .images(List.of(second, first))
                .isActive(true)
                .build();

        when(experienceRepository.findByIsActiveTrue()).thenReturn(List.of(experience));
        when(experienceScheduleRepository.findByExperienceIdAndIsActiveTrue(experience.getId()))
                .thenReturn(List.of());

        List<ExperienceResponse> responses = experienceService.getActiveExperienceResponses();

        assertThat(responses.get(0).getImages())
                .extracting("displayOrder")
                .containsExactly(1, 2);
    }

    @Test
    void treatsNullDisplayOrderAsLowestWhenSorting() {
        ExperienceImage withOrder = mock(ExperienceImage.class);
        when(withOrder.getId()).thenReturn(1L);
        when(withOrder.getImageUrl()).thenReturn("https://example.com/with-order.jpg");
        when(withOrder.getDisplayOrder()).thenReturn(5);

        ExperienceImage withNullOrder = mock(ExperienceImage.class);
        when(withNullOrder.getId()).thenReturn(2L);
        when(withNullOrder.getImageUrl()).thenReturn("https://example.com/null-order.jpg");
        when(withNullOrder.getDisplayOrder()).thenReturn(null);

        Experience experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER.name())
                .supportedLanguages(List.of("ko", "en"))
                .locationAddress("서울 종로구")
                .images(List.of(withOrder, withNullOrder))
                .isActive(true)
                .build();

        when(experienceRepository.findByIsActiveTrue()).thenReturn(List.of(experience));
        when(experienceScheduleRepository.findByExperienceIdAndIsActiveTrue(experience.getId()))
                .thenReturn(List.of());

        List<ExperienceResponse> responses = experienceService.getActiveExperienceResponses();

        assertThat(responses.get(0).getImages())
                .extracting("id")
                .containsExactly(2L, 1L);
    }

    @Test
    void appendsExperienceImagesAfterExistingDisplayOrder() {
        ExperienceImage existingImage = ExperienceImage.builder()
                .imageUrl("https://example.com/main.jpg")
                .displayOrder(2)
                .build();
        Experience experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER.name())
                .supportedLanguages(List.of("ko", "en"))
                .locationAddress("서울 종로구")
                .images(new java.util.ArrayList<>(List.of(existingImage)))
                .isActive(true)
                .build();
        AddExperienceImagesRequest request = AddExperienceImagesRequest.builder()
                .imageUrls(List.of(" https://example.com/detail-1.jpg ", "https://example.com/detail-2.jpg"))
                .build();

        when(experienceRepository.findById(experience.getId())).thenReturn(Optional.of(experience));
        when(experienceImageRepository.findMaxDisplayOrderByExperienceId(experience.getId())).thenReturn(2);
        when(experienceScheduleRepository.findByExperienceIdAndIsActiveTrue(experience.getId())).thenReturn(List.of());

        ExperienceResponse response = experienceService.addExperienceImages(experience.getId(), artisan.getId(), request);

        assertThat(response.getImages())
                .extracting("imageUrl")
                .containsExactly(
                        "https://example.com/main.jpg",
                        "https://example.com/detail-1.jpg",
                        "https://example.com/detail-2.jpg"
                );
        assertThat(response.getImages())
                .extracting("displayOrder")
                .containsExactly(2, 3, 4);
        verify(experienceImageRepository).saveAll(anyList());
    }

    @Test
    void deletesDependentRowsBeforeDeletingExperience() {
        Experience experience = Experience.builder()
                .id(100L)
                .artisan(artisan)
                .title("전통 매듭 만들기")
                .description("전통 매듭을 배웁니다")
                .category("공예")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(90)
                .maxParticipants(8)
                .difficulty(ExperienceDifficulty.BEGINNER.name())
                .supportedLanguages(List.of("ko", "en"))
                .locationAddress("서울 종로구")
                .isActive(true)
                .build();

        when(experienceRepository.findById(experience.getId())).thenReturn(Optional.of(experience));

        experienceService.deleteExperience(experience.getId(), artisan.getId());

        org.mockito.InOrder ordered = inOrder(
                reviewRepository,
                reservationRepository,
                wishlistRepository,
                cardNewsExperienceRepository,
                experienceScheduleRepository,
                experienceImageRepository,
                experienceRepository
        );
        ordered.verify(reviewRepository).deleteCollectionsByExperienceId(experience.getId());
        ordered.verify(reviewRepository).deleteByExperienceId(experience.getId());
        ordered.verify(reservationRepository).deleteByExperienceId(experience.getId());
        ordered.verify(wishlistRepository).deleteByExperienceId(experience.getId());
        ordered.verify(cardNewsExperienceRepository).deleteByExperienceId(experience.getId());
        ordered.verify(experienceScheduleRepository).deleteByExperienceId(experience.getId());
        ordered.verify(experienceImageRepository).deleteAllByExperienceId(experience.getId());
        ordered.verify(experienceRepository).deleteSupportedLanguagesByExperienceId(experience.getId());
        ordered.verify(experienceRepository).deleteTagsByExperienceId(experience.getId());
        ordered.verify(experienceRepository).delete(experience);
    }
}
