package com.szakdolgozat.scheduler.vehiclerouting.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Getter
@Setter
public class Vehicle {

    @PlanningId
    @NotNull
    private Long id;

    @NotBlank
    private String name;
    @Positive
    private int capacity;
    @NotNull
    private Location depot;

    public Vehicle() {
    }

    public Vehicle(Long id, String name, int capacity, Location depot) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.depot = depot;
    }
}
