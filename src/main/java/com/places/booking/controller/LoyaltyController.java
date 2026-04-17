package com.places.booking.controller;

import com.places.booking.dto.LoyaltyDtos;
import com.places.booking.service.LoyaltyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/me")
    public LoyaltyDtos.LoyaltyMeResponse me() {
        return loyaltyService.getCurrentUserSummary();
    }

    @GetMapping("/rewards")
    public List<LoyaltyDtos.RewardResponse> rewards() {
        return loyaltyService.listActiveRewards();
    }

    @PostMapping("/rewards/{id}/redeem")
    public LoyaltyDtos.RedeemResponse redeem(@PathVariable("id") long id) {
        return loyaltyService.redeem(id);
    }
}
