package com.janginharou.domain.settlement.repository;

import com.janginharou.domain.settlement.entity.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {

    List<SettlementItem> findBySettlementId(Long settlementId);

    boolean existsByReservationId(Long reservationId);
}
