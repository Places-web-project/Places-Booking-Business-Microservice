package com.places.booking.repository;

import com.places.booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("select r from Room r where lower(r.name) like lower(concat('%', :search, '%'))")
    List<Room> searchByName(String search);
}
