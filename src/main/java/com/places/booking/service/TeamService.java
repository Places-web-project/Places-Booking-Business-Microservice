package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
import com.places.booking.model.Team;
import com.places.booking.model.TeamMember;
import com.places.booking.repository.TeamMemberRepository;
import com.places.booking.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public PagedResponse<BookingDtos.TeamResponse> findAll(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Team> teams = (search == null || search.isBlank())
                ? teamRepository.findAll(pageable)
                : teamRepository.searchByName(search, pageable);
        return PagedResponse.of(teams.map(this::toResponse));
    }

    public BookingDtos.TeamResponse findById(Long id) {
        return toResponse(teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found")));
    }

    public BookingDtos.TeamResponse create(BookingDtos.TeamRequest request) {
        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        return toResponse(teamRepository.save(team));
    }

    public BookingDtos.TeamResponse update(Long id, BookingDtos.TeamRequest request) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        team.setName(request.name());
        team.setDescription(request.description());
        return toResponse(teamRepository.save(team));
    }

    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("Team not found");
        }
        teamRepository.deleteById(id);
    }

    public List<BookingDtos.TeamMemberResponse> getMembers(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("Team not found");
        }
        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    public BookingDtos.TeamMemberResponse addMember(Long teamId, BookingDtos.TeamMemberRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        if (teamMemberRepository.existsByUserIdAndTeamId(request.userId(), teamId)) {
            throw new IllegalArgumentException("User is already a member of this team");
        }
        TeamMember member = new TeamMember();
        member.setUserId(request.userId());
        member.setDisplayName(request.displayName());
        member.setTeam(team);
        return toMemberResponse(teamMemberRepository.save(member));
    }

    public void removeMember(Long teamId, Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Member does not belong to this team");
        }
        teamMemberRepository.deleteById(memberId);
    }

    private BookingDtos.TeamResponse toResponse(Team team) {
        return new BookingDtos.TeamResponse(team.getId(), team.getName(), team.getDescription());
    }

    private BookingDtos.TeamMemberResponse toMemberResponse(TeamMember m) {
        return new BookingDtos.TeamMemberResponse(m.getId(), m.getUserId(), m.getDisplayName(), m.getTeam().getId());
    }
}
