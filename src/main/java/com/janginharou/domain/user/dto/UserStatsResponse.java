package com.janginharou.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {

    private Long wishlistCount;
    private Long reviewCount;
    private Long couponCount;
    private Long pointBalance;

    public static UserStatsResponse of(Long wishlistCount, Long reviewCount, Long couponCount, Long pointBalance) {
        return UserStatsResponse.builder()
                .wishlistCount(wishlistCount)
                .reviewCount(reviewCount)
                .couponCount(couponCount)
                .pointBalance(pointBalance)
                .build();
    }
}
