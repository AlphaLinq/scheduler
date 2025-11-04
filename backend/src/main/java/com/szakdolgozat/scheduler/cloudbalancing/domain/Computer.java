package com.szakdolgozat.scheduler.cloudbalancing.domain;

import lombok.Getter;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Getter
public class Computer {

    @PlanningId
    private int id;
    private int cpuPower;
    private int memory;
    private int networkBandwidth;
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
