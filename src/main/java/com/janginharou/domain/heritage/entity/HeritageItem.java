package com.janginharou.domain.heritage.entity;

import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "heritage_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeritageItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String source;  // 문화재청, 국가유산포털 등

    @Column(unique = true, length = 100)
    private String externalId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String history;

    // vector(1536) — pgvector 타입. JPA 직접 매핑 시 pgvector Hibernate 타입 등록 필요.
    // 벡터 연산은 JDBC 또는 네이티브 쿼리로 처리.
    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;
}
