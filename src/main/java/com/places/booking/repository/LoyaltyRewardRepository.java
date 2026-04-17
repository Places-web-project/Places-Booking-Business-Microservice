package com.places.booking.repository;

import com.places.booking.model.LoyaltyReward;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoyaltyRewardRepository extends JpaRepository<LoyaltyReward, Long> {

    List<LoyaltyReward> findByActiveTrueOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM LoyaltyReward r WHERE r.id = :id AND r.active = true")
    Optional<LoyaltyReward> findActiveByIdForUpdate(@Param("id") Long id);
}
