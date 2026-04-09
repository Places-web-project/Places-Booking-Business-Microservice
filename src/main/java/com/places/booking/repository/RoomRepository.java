package com.places.booking.repository;

import com.places.booking.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("select r from Room r where lower(r.name) like lower(concat('%', :search, '%'))")
    Page<Room> searchByName(String search, Pageable pageable);

    Page<Room> findAll(Pageable pageable);
}
