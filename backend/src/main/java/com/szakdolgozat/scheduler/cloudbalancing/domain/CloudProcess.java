package com.szakdolgozat.scheduler.cloudbalancing.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@Getter
@PlanningEntity
public class CloudProcess {

    @PlanningId
    @NotNull
    private Long id;

    @Positive
    private int requiredCpuPower;
    @Positive
    private int requiredMemory;
    @Positive
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
