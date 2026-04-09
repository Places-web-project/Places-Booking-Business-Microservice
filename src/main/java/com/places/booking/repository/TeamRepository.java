package com.places.booking.repository;

import com.places.booking.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Page<Team> findAll(Pageable pageable);

    @Query("select t from Team t where lower(t.name) like lower(concat('%', :search, '%'))")
    Page<Team> searchByName(String search, Pageable pageable);
}
