package com.szakdolgozat.scheduler.cloudbalancing.domain;

import lombok.Getter;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Getter
public class Computer {


    private int cpuPower;
    private int memory;
    private int networkBandwidth;
    private int cost;

    public Computer() {
    }

    public Computer(int cpuPower, int memory, int networkBandwidth, int cost) {
        this.cpuPower = cpuPower;
        this.memory = memory;
        this.networkBandwidth = networkBandwidth;
        this.cost = cost;
    }

}
