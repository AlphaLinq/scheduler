package com.szakdolgozat.scheduler.vehiclerouting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRoutingSolutionWithIterations {
    private VehicleRoutingSolution finalSolution;
    private List<VehicleRoutingIteration> iterations;
}
