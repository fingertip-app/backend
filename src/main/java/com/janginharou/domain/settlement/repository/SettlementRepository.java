package com.janginharou.domain.settlement.repository;

import com.janginharou.domain.settlement.entity.Settlement;
import com.janginharou.domain.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByArtisanId(Long artisanId);

    List<Settlement> findByArtisanIdOrderByCreatedAtDesc(Long artisanId);

    List<Settlement> findByStatus(SettlementStatus status);
}
