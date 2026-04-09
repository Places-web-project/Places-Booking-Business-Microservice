package com.places.booking.repository;

import com.places.booking.model.Desk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeskRepository extends JpaRepository<Desk, Long> {

    Page<Desk> findByRoomId(Long roomId, Pageable pageable);

    @Query("select d from Desk d where d.room.id = :roomId and lower(d.code) like lower(concat('%', :search, '%'))")
    Page<Desk> searchByRoomIdAndCode(Long roomId, String search, Pageable pageable);

    boolean existsByCodeAndRoomId(String code, Long roomId);
}
