package com.places.booking.dto;

import java.util.List;

public final class LoyaltyDtos {

    private LoyaltyDtos() {
    }

    public record EarningRuleItem(
            String label,
            int points,
            String description,
            boolean implemented
    ) {
    }

    public record LoyaltyMeResponse(
            long balance,
            int nextMilestone,
            int attendanceRank,
            long rankPoolSize,
            boolean isTopThree,
            List<EarningRuleItem> earningRules
    ) {
    }

    public record RewardResponse(
            Long id,
            String name,
            String description,
            int pointsCost,
            String category,
            int stockRemaining,
            String iconKey,
            boolean active
    ) {
    }

    public record RedeemResponse(long balanceAfter, String message) {
    }
}
