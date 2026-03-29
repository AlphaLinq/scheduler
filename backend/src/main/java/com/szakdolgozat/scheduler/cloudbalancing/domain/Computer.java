package com.szakdolgozat.scheduler.cloudbalancing.domain;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Getter
public class Computer {

    @PlanningId
    private int id;
    @Positive
    private int cpuPower;
    @Positive
    private int memory;
    @Positive
    private int networkBandwidth;
    @Positive
    private int cost;

    public Computer() {
    }

    public Computer(int id, int cpuPower, int memory, int networkBandwidth, int cost) {
        this.id = id;
        this.cpuPower = cpuPower;
        this.memory = memory;
        this.networkBandwidth = networkBandwidth;
        this.cost = cost;
    }

}
