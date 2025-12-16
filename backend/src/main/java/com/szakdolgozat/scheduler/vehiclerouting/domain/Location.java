package com.szakdolgozat.scheduler.vehiclerouting.domain;

import lombok.Getter;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Getter
public class Location {

    @PlanningId
    private Long id;

    private String name;
    private double latitude;
    private double longitude;

    public Location() {
    }

    public Location(Long id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Calculate distance to another location using Euclidean distance
     */
    public double getDistanceTo(Location other) {
        double latDiff = this.latitude - other.latitude;
        double lonDiff = this.longitude - other.longitude;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111.0; // Approximate km conversion
    }
}

