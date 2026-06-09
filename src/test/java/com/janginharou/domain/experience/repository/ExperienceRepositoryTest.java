package com.janginharou.domain.experience.repository;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.dao.DataIntegrityViolationException;
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
    ExperienceSchedule.class
})
@EnableJpaRepositories(basePackageClasses = {
    UserRepository.class,
    ArtisanRepository.class,
    ExperienceRepository.class
})
@Import(JpaAuditingConfig.class)
class ExperienceRepositoryTest {

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private ArtisanRepository artisanRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Artisan testArtisan;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
            .email("test@example.com")
            .provider("test")
            .nickname("Test User")
            .isActive(true)
            .build();
        userRepository.save(testUser);

        // 테스트용 장인 생성
        testArtisan = Artisan.builder()
            .user(testUser)
            .name("Test Artisan")
            .heritageCategory("전통공예")
            .isVerified(true)
            .isActive(true)
            .build();
        artisanRepository.save(testArtisan);
    }

    @Test
    void findByTagsContainingExactMatch() {
        // Given: 태그가 있는 체험
        Experience exp1 = Experience.builder()
            .artisan(testArtisan)
            .title("매듭 체험")
            .description("전통 매듭 배우기")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예", "매듭"))
            .isActive(true)
            .build();

        Experience exp2 = Experience.builder()
            .artisan(testArtisan)
            .title("도예 체험")
            .description("도자기 만들기")
            .category("도예")
            .price(BigDecimal.valueOf(60000))
            .durationMinutes(150)
            .maxParticipants(8)
            .tags(List.of("도예"))
            .isActive(true)
            .build();

        experienceRepository.saveAll(List.of(exp1, exp2));

        // When
        List<Experience> results = experienceRepository.findByTagsContaining("공예");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("매듭 체험");
        assertThat(results.get(0).getTags()).contains("공예");
    }

    @Test
    void findByTagsContainingReturnsEmptyWhenNotFound() {
        // Given: 어떤 체험도 저장되지 않음
        // When
        List<Experience> results = experienceRepository.findByTagsContaining("없는태그");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByTagsContainingAnyMultipleTagsOrderedByMatchCount() {
        // Given: 다양한 태그를 가진 체험
        Experience exp1 = Experience.builder()
            .artisan(testArtisan)
            .title("매듭 체험")
            .description("전통 매듭 배우기")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예", "매듭"))
            .isActive(true)
            .build();

        Experience exp2 = Experience.builder()
            .artisan(testArtisan)
            .title("도자기 체험")
            .description("도자기 만들기")
            .category("도예")
            .price(BigDecimal.valueOf(60000))
            .durationMinutes(150)
            .maxParticipants(8)
            .tags(List.of("공예"))
            .isActive(true)
            .build();

        experienceRepository.saveAll(List.of(exp1, exp2));

        // When: 공예, 매듭 태그 검색
        List<Experience> results = experienceRepository.findByTagsContainingAny(
            List.of("공예", "매듭")
        );

        // Then: exp1이 먼저 (2개 매칭), exp2는 나중 (1개 매칭)
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("매듭 체험");
        assertThat(results.get(1).getTitle()).isEqualTo("도자기 체험");
    }

    @Test
    void findByTagsContainingAnyIgnoresInactiveExperiences() {
        // Given: 비활성 체험
        Experience inactive = Experience.builder()
            .artisan(testArtisan)
            .title("폐지된 체험")
            .description("더 이상 제공하지 않음")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예"))
            .isActive(false)
            .build();

        Experience active = Experience.builder()
            .artisan(testArtisan)
            .title("활성 체험")
            .description("진행중인 체험")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예"))
            .isActive(true)
            .build();

        experienceRepository.saveAll(List.of(inactive, active));

        // When
        List<Experience> results = experienceRepository.findByTagsContainingAny(
            List.of("공예")
        );

        // Then: 비활성 체험 제외
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("활성 체험");
    }

    @Test
    void findByTagsContainingAnyWithEmptyTagListReturnsEmpty() {
        // Given: 체험이 저장되어 있음
        Experience exp = Experience.builder()
            .artisan(testArtisan)
            .title("매듭 체험")
            .description("전통 매듭 배우기")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예"))
            .isActive(true)
            .build();
        experienceRepository.save(exp);

        // When: 빈 태그 리스트로 검색
        List<Experience> results = experienceRepository.findByTagsContainingAny(List.of());

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByTagsContainingAnyNoDuplicates() {
        // Given: 여러 태그를 가진 체험
        Experience exp = Experience.builder()
            .artisan(testArtisan)
            .title("매듭 체험")
            .description("전통 매듭 배우기")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예", "매듭", "전통"))
            .isActive(true)
            .build();
        experienceRepository.save(exp);

        // When: 체험의 모든 태그로 검색
        List<Experience> results = experienceRepository.findByTagsContainingAny(
            List.of("공예", "매듭", "전통")
        );

        // Then: 중복 없이 1개만 반환
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("매듭 체험");
    }

    @Test
    void duplicateTagForSameExperienceViolatesUniqueConstraint() {
        Experience experience = Experience.builder()
            .artisan(testArtisan)
            .title("중복 태그 체험")
            .description("동일 체험에 같은 태그가 두 번 포함됨")
            .category("공예")
            .price(BigDecimal.valueOf(50000))
            .durationMinutes(120)
            .maxParticipants(10)
            .tags(List.of("공예", "공예"))
            .isActive(true)
            .build();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> experienceRepository.saveAndFlush(experience)
            )
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
