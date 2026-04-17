package com.places.booking.service;

import com.places.booking.dto.LoyaltyDtos;
import com.places.booking.model.Booking;
import com.places.booking.model.LoyaltyReward;
import com.places.booking.model.LoyaltyTransaction;
import com.places.booking.model.LoyaltyTransactionType;
import com.places.booking.repository.BookingRepository;
import com.places.booking.repository.LoyaltyRewardRepository;
import com.places.booking.repository.LoyaltyTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class LoyaltyService {

    public static final int DESK_CHECKIN_POINTS = 10;
    public static final int DEFAULT_NEXT_MILESTONE = 1000;
    private static final int LEADERBOARD_WINDOW_DAYS = 30;

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final LoyaltyRewardRepository loyaltyRewardRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;

    public LoyaltyService(
            LoyaltyTransactionRepository loyaltyTransactionRepository,
            LoyaltyRewardRepository loyaltyRewardRepository,
            BookingRepository bookingRepository,
            CurrentUserService currentUserService
    ) {
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
        this.loyaltyRewardRepository = loyaltyRewardRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Call after a successful desk check-in. Idempotent per booking via unique partial index.
     */
    @Transactional
    public void recordDeskCheckinEarnIfApplicable(Booking booking) {
        if (booking.getRoom() == null) {
            return;
        }
        String t = booking.getRoom().getRoomType();
        if (t == null || t.isBlank()) {
            return;
        }
        if (!"DESK".equals(t.trim().toUpperCase(Locale.ROOT))) {
            return;
        }
        Long bid = booking.getId();
        if (bid == null) {
            return;
        }
        if (loyaltyTransactionRepository.existsByBookingId(bid)) {
            return;
        }

        LoyaltyTransaction tx = new LoyaltyTransaction(
                booking.getUserId(),
                DESK_CHECKIN_POINTS,
                LoyaltyTransactionType.DESK_CHECKIN,
                bid,
                null,
                LocalDateTime.now()
        );
        loyaltyTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public LoyaltyDtos.LoyaltyMeResponse getCurrentUserSummary() {
        Long userId = currentUserService.requireUserId();
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(LEADERBOARD_WINDOW_DAYS);

        long balance = loyaltyTransactionRepository.sumAmountByUserId(userId);
        long myDeskCount = bookingRepository.countDeskCheckInsForUser(userId, from, to);
        long strictlyAhead = bookingRepository.countUsersStrictlyAheadInDeskLeaderboard(myDeskCount, from, to);
        int rank = (int) (strictlyAhead + 1);
        long poolSize = bookingRepository.countDistinctUsersInDeskLeaderboard(from, to);

        boolean isTopThree = rank <= 3;

        List<LoyaltyDtos.EarningRuleItem> rules = List.of(
                new LoyaltyDtos.EarningRuleItem("Desk booking check-in", 10, "Per approved desk booking you check in to", true),
                new LoyaltyDtos.EarningRuleItem("Weekly check-in bonus", 50, "Awarded once per calendar week", false),
                new LoyaltyDtos.EarningRuleItem("Monthly attendance", 500, "Based on consistent office presence", false)
        );

        return new LoyaltyDtos.LoyaltyMeResponse(
                balance,
                DEFAULT_NEXT_MILESTONE,
                rank,
                poolSize,
                isTopThree,
                rules
        );
    }

    @Transactional(readOnly = true)
    public List<LoyaltyDtos.RewardResponse> listActiveRewards() {
        return loyaltyRewardRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(this::toRewardResponse)
                .toList();
    }

    @Transactional
    public LoyaltyDtos.RedeemResponse redeem(long rewardId) {
        Long userId = currentUserService.requireUserId();

        LoyaltyReward reward = loyaltyRewardRepository.findActiveByIdForUpdate(rewardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reward not found"));

        if (reward.getStockRemaining() == null || reward.getStockRemaining() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reward is out of stock");
        }

        long balance = loyaltyTransactionRepository.sumAmountByUserId(userId);
        int cost = reward.getPointsCost();
        if (balance < cost) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient points");
        }

        reward.setStockRemaining(reward.getStockRemaining() - 1);
        loyaltyRewardRepository.save(reward);

        LoyaltyTransaction redeemTx = new LoyaltyTransaction(
                userId,
                -cost,
                LoyaltyTransactionType.REDEEM,
                null,
                reward.getId(),
                LocalDateTime.now()
        );
        loyaltyTransactionRepository.save(redeemTx);

        long newBalance = loyaltyTransactionRepository.sumAmountByUserId(userId);
        return new LoyaltyDtos.RedeemResponse(newBalance, "Reward redeemed");
    }

    private LoyaltyDtos.RewardResponse toRewardResponse(LoyaltyReward r) {
        return new LoyaltyDtos.RewardResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getPointsCost(),
                r.getCategory(),
                r.getStockRemaining(),
                r.getIconKey(),
                r.isActive()
        );
    }
}
