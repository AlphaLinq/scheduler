package com.szakdolgozat.scheduler.vehiclerouting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRoutingIteration {
    private int stepCount;
    private long timeMillis;
    private HardSoftScore score;
    private VehicleRoutingSolution solution;
    private String phaseName;
    private Map<String, String> constraintScores;
}
