package com.szakdolgozat.scheduler.timetable.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableIteration {
    private int stepCount;
    private long timeMillis;
    private HardSoftScore score;
    private TimeTable solution;
    private String phaseName;
    private Map<String, String> constraintScores;
}
