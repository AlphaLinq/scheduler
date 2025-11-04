package com.szakdolgozat.scheduler.cloudbalancing.domain;

import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@Getter
@PlanningEntity
public class CloudProcess {

    @PlanningId
    private Long id;

    private int requiredCpuPower;
    private int requiredMemory;
    private int requiredBandwidth;

    @PlanningVariable(valueRangeProviderRefs = "computerList")
    @Setter
    private Computer computer;

    public CloudProcess() {
    }

    public CloudProcess(Long id, int requiredCpuPower, int requiredMemory, int requiredBandwidth, Computer computer) {
        this.id = id;
        this.requiredCpuPower = requiredCpuPower;
        this.requiredMemory = requiredMemory;
        this.requiredBandwidth = requiredBandwidth;
        this.computer = computer;
    }

    public Computer getComputer() {
        return computer;
    }

}
