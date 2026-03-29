package com.szakdolgozat.scheduler.cloudbalancing.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@Getter
@PlanningSolution
public class CloudBalance {

    @JsonProperty("processList")
    @PlanningEntityCollectionProperty
    @NotEmpty
    @Valid
    private List<CloudProcess> cloudProcessList;

    @ValueRangeProvider(id = "computerList")
    @ProblemFactCollectionProperty
    @NotEmpty
    @Valid
    private List<Computer> computerList;

    @PlanningScore
    private HardSoftScore score;

    public CloudBalance() {
    }

    public CloudBalance(List<CloudProcess> cloudProcessList, List<Computer> computerList) {
        this.cloudProcessList = cloudProcessList;
        this.computerList = computerList;
    }
}
