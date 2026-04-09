package com.places.booking.controller;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public PagedResponse<BookingDtos.TeamResponse> getTeams(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return teamService.findAll(search, page, size);
    }

    @GetMapping("/{id}")
    public BookingDtos.TeamResponse getTeam(@PathVariable Long id) {
        return teamService.findById(id);
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

    // ---- Members nested under a team ----

    @GetMapping("/{teamId}/members")
    public List<BookingDtos.TeamMemberResponse> getMembers(@PathVariable Long teamId) {
        return teamService.getMembers(teamId);
    }

    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.TeamMemberResponse addMember(
            @PathVariable Long teamId,
            @Valid @RequestBody BookingDtos.TeamMemberRequest request
    ) {
        return teamService.addMember(teamId, request);
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        teamService.removeMember(teamId, memberId);
    }
}
