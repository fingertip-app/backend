package com.janginharou.domain.experience.repository;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceImage;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackageClasses = {
    User.class,
    Artisan.class,
    Experience.class,
    ExperienceSchedule.class,
    ExperienceImage.class
})
@EnableJpaRepositories(basePackageClasses = {
    UserRepository.class,
    ArtisanRepository.class,
    ExperienceRepository.class,
    ExperienceImageRepository.class
})
@Import(JpaAuditingConfig.class)
class ExperienceImageMappingTest {

    @Autowired
    private ExperienceImageRepository experienceImageRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private ArtisanRepository artisanRepository;

    @Autowired
    private UserRepository userRepository;

    private Artisan testArtisan;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
            .email("test@example.com")
            .provider("test")
            .nickname("Test User")
            .isActive(true)
            .build();
        userRepository.save(testUser);

        testArtisan = Artisan.builder()
            .user(testUser)
            .name("Test Artisan")
            .heritageCategory("도자기")
            .isVerified(true)
            .isActive(true)
            .build();
        artisanRepository.save(testArtisan);
    }

    @Test
    void 체험에_이미지를_추가하면_순서대로_조회된다() {
        // Given
        Experience experience = Experience.builder()
            .artisan(testArtisan)
            .title("도자기 체험")
            .description("물레로 도자기 만들기")
            .category("도자기")
            .price(BigDecimal.valueOf(35000))
            .durationMinutes(120)
            .maxParticipants(4)
            .isActive(true)
            .build();

        experience.addImage(ExperienceImage.builder()
            .imageUrl("https://placeholder.test/img1.jpg")
            .displayOrder(0)
            .build());
        experience.addImage(ExperienceImage.builder()
            .imageUrl("https://placeholder.test/img2.jpg")
            .displayOrder(1)
            .build());
        experience.addImage(ExperienceImage.builder()
            .imageUrl("https://placeholder.test/img3.jpg")
            .displayOrder(2)
            .build());

        experienceRepository.save(experience);

        // When
        Experience found = experienceRepository.findById(experience.getId()).orElseThrow();

        // Then
        assertThat(found.getImages()).hasSize(3);
        assertThat(found.getImages().get(0).getImageUrl()).isEqualTo("https://placeholder.test/img1.jpg");
        assertThat(found.getImages().get(2).getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void 체험을_삭제하면_연관된_이미지도_같이_삭제된다() {
        // Given
        Experience experience = Experience.builder()
            .artisan(testArtisan)
            .title("도자기 체험")
            .description("물레로 도자기 만들기")
            .category("도자기")
            .price(BigDecimal.valueOf(35000))
            .durationMinutes(120)
            .maxParticipants(4)
            .isActive(true)
            .build();

        experience.addImage(ExperienceImage.builder()
            .imageUrl("https://placeholder.test/img1.jpg")
            .displayOrder(0)
            .build());

        experienceRepository.save(experience);
        Long experienceId = experience.getId();

        // When
        experienceRepository.deleteById(experienceId);
        experienceRepository.flush();

        // Then
        assertThat(experienceRepository.findById(experienceId)).isEmpty();
        assertThat(experienceImageRepository.findByExperienceIdOrderByDisplayOrderAsc(experienceId)).isEmpty();
    }
}