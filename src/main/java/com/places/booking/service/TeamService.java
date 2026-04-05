package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.model.Team;
import com.places.booking.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<BookingDtos.TeamResponse> findAll() {
        return teamRepository.findAll().stream().map(this::toResponse).toList();
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
        teamRepository.deleteById(id);
    }

    private BookingDtos.TeamResponse toResponse(Team team) {
        return new BookingDtos.TeamResponse(team.getId(), team.getName(), team.getDescription());
    }
}
