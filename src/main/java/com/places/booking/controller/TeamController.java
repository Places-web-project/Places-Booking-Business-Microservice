package com.places.booking.controller;

import com.places.booking.dto.BookingDtos;
import com.places.booking.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<BookingDtos.TeamResponse> getTeams() {
        return teamService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.TeamResponse createTeam(@Valid @RequestBody BookingDtos.TeamRequest request) {
        return teamService.create(request);
    }

    @PutMapping("/{id}")
    public BookingDtos.TeamResponse updateTeam(@PathVariable Long id, @Valid @RequestBody BookingDtos.TeamRequest request) {
        return teamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable Long id) {
        teamService.delete(id);
    }
}
