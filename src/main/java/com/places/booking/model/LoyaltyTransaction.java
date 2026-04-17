package com.places.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transaction")
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 40)
    private LoyaltyTransactionType transactionType;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "reward_id")
    private Long rewardId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public LoyaltyTransaction() {
    }

    public LoyaltyTransaction(
            Long userId,
            Integer amount,
            LoyaltyTransactionType transactionType,
            Long bookingId,
            Long rewardId,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.bookingId = bookingId;
        this.rewardId = rewardId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getAmount() {
        return amount;
    }

    public LoyaltyTransactionType getTransactionType() {
        return transactionType;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getRewardId() {
        return rewardId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
