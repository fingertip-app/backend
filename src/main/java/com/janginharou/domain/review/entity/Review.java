package com.janginharou.domain.review.entity;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String newLearnings;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String contentEn;

    @ElementCollection
    @CollectionTable(name = "review_image_urls", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls;

    @Column(precision = 3, scale = 2)
    private BigDecimal sentimentScore;

    @ElementCollection
    @CollectionTable(name = "review_keywords", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "keyword", length = 100)
    private List<String> keywords;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String replyContent;

    @Column
    private LocalDateTime repliedAt;

    public void applySummary(String summary, BigDecimal sentimentScore, List<String> keywords) {
        this.summary = summary;
        this.sentimentScore = sentimentScore;
        this.keywords = keywords;
    }

    public void update(Integer rating, String content, String newLearnings, List<String> imageUrls) {
        this.rating = rating;
        this.content = content;
        this.newLearnings = newLearnings;
        this.imageUrls = imageUrls;
        this.summary = null;
        this.sentimentScore = null;
        this.keywords = null;
    }

    public void createReply(String replyContent) {
        this.replyContent = replyContent;
        this.repliedAt = LocalDateTime.now();
    }

    public void updateReply(String replyContent) {
        this.replyContent = replyContent;
        this.repliedAt = LocalDateTime.now();
    }

    public void deleteReply() {
        this.replyContent = null;
        this.repliedAt = null;
    }
}
