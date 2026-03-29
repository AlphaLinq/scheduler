package com.szakdolgozat.scheduler.vehiclerouting.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@Getter
@PlanningEntity
public class Customer {

    @PlanningId
    @NotNull
    private Long id;

    @NotBlank
    private String name;
    @NotNull
    private Location location;
    @Positive
    private int demand;
    private int serviceDuration; // in minutes
    private Integer readyTime; // in minutes from depot start, null if no constraint
    private Integer dueTime; // in minutes from depot start, null if no constraint

    @PlanningVariable(valueRangeProviderRefs = "vehicleRange")
    @Setter
    private Vehicle vehicle;

    public Customer() {
    }

    public Customer(Long id, String name, Location location, int demand) {
        this(id, name, location, demand, 15, null, null); // default 15 min service, no time windows
    }

    public Customer(Long id, String name, Location location, int demand, int serviceDuration, Integer readyTime, Integer dueTime) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.demand = demand;
        this.serviceDuration = serviceDuration;
        this.readyTime = readyTime;
        this.dueTime = dueTime;
    }

    public double getDistanceFromDepot() {
        if (vehicle == null) {
            return 0.0;
        }
        return vehicle.getDepot().getDistanceTo(location);
    }

    // Calculate travel time in minutes (assuming 60 km/h average speed)
    public int getTravelTimeFromDepot() {
        return (int) (getDistanceFromDepot() * 60.0 / 60.0); // distance in km, speed 60 km/h
    }
}
