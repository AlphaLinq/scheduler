package com.szakdolgozat.scheduler.cloudbalancing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloudBalanceIteration {
    private int stepCount;
    private long timeMillis;
    private HardSoftScore score;
    private CloudBalance solution;
    private String phaseName;
    private Map<String, String> constraintScores;
}
