package com.places.booking.repository;

import com.places.booking.model.Booking;
import com.places.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query("select b from Booking b where b.room.id = :roomId")
    Page<Booking> findByRoomId(Long roomId, Pageable pageable);
}
