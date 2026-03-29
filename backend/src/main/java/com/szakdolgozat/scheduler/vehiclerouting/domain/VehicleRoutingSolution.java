package com.szakdolgozat.scheduler.vehiclerouting.domain;

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
public class VehicleRoutingSolution {

    @JsonProperty("customerList")
    @PlanningEntityCollectionProperty
    @NotEmpty
    @Valid
    private List<Customer> customerList;

    @ValueRangeProvider(id = "vehicleRange")
    @ProblemFactCollectionProperty
    @NotEmpty
    @Valid
    private List<Vehicle> vehicleList;

    @ProblemFactCollectionProperty
    @NotEmpty
    @Valid
    private List<Location> locationList;

    @PlanningScore
    private HardSoftScore score;

    public VehicleRoutingSolution() {
    }

    public VehicleRoutingSolution(List<Customer> customerList, List<Vehicle> vehicleList, List<Location> locationList) {
        this.customerList = customerList;
        this.vehicleList = vehicleList;
        this.locationList = locationList;
    }
}

