package com.places.booking.repository;

import com.places.booking.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM LoyaltyTransaction t WHERE t.userId = :userId")
    long sumAmountByUserId(@Param("userId") Long userId);
}
