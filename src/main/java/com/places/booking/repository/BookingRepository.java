package com.places.booking.repository;

import com.places.booking.model.Booking;
import com.places.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.room.id = :roomId")
    Page<Booking> findByRoomId(Long roomId, Pageable pageable);

    long countByUserIdAndStatusAndEndsAtBetween(
            Long userId,
            BookingStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    long countByUserIdAndStatusAndEndsAtBetweenAndCheckedInAtIsNotNull(
            Long userId,
            BookingStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query(value = """
            SELECT COUNT(*) FROM booking_schema.bookings b
            INNER JOIN booking_schema.rooms r ON r.id = b.room_id
            WHERE b.user_id = :userId
              AND b.status = 'APPROVED'
              AND b.checked_in_at IS NOT NULL
              AND UPPER(TRIM(COALESCE(r.room_type, ''))) = 'DESK'
              AND b.ends_at BETWEEN :fromTs AND :toTs
            """, nativeQuery = true)
    long countDeskCheckInsForUser(
            @Param("userId") Long userId,
            @Param("fromTs") LocalDateTime fromTs,
            @Param("toTs") LocalDateTime toTs
    );

    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT b.user_id FROM booking_schema.bookings b
                INNER JOIN booking_schema.rooms r ON r.id = b.room_id
                WHERE b.status = 'APPROVED'
                  AND b.checked_in_at IS NOT NULL
                  AND UPPER(TRIM(COALESCE(r.room_type, ''))) = 'DESK'
                  AND b.ends_at BETWEEN :fromTs AND :toTs
                GROUP BY b.user_id
                HAVING COUNT(*) > :myCount
            ) ranked
            """, nativeQuery = true)
    long countUsersStrictlyAheadInDeskLeaderboard(
            @Param("myCount") long myCount,
            @Param("fromTs") LocalDateTime fromTs,
            @Param("toTs") LocalDateTime toTs
    );

    @Query(value = """
            SELECT COUNT(DISTINCT b.user_id) FROM booking_schema.bookings b
            INNER JOIN booking_schema.rooms r ON r.id = b.room_id
            WHERE b.status = 'APPROVED'
              AND b.checked_in_at IS NOT NULL
              AND UPPER(TRIM(COALESCE(r.room_type, ''))) = 'DESK'
              AND b.ends_at BETWEEN :fromTs AND :toTs
            """, nativeQuery = true)
    long countDistinctUsersInDeskLeaderboard(
            @Param("fromTs") LocalDateTime fromTs,
            @Param("toTs") LocalDateTime toTs
    );
}
