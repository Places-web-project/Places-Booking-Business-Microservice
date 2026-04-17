package com.places.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loyalty_reward")
public class LoyaltyReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "stock_remaining", nullable = false)
    private Integer stockRemaining;

    @Column(name = "icon_key", nullable = false, length = 80)
    private String iconKey;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public String getCategory() {
        return category;
    }

    public Integer getStockRemaining() {
        return stockRemaining;
    }

    public void setStockRemaining(Integer stockRemaining) {
        this.stockRemaining = stockRemaining;
    }

    public String getIconKey() {
        return iconKey;
    }

    public boolean isActive() {
        return active;
    }
}
