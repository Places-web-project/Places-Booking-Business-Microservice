package com.places.booking.repository;

import com.places.booking.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

    boolean existsByUserIdAndTeamId(Long userId, Long teamId);
}
