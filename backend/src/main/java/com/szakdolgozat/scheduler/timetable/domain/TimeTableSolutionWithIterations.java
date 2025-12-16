package com.szakdolgozat.scheduler.timetable.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableSolutionWithIterations {
    private TimeTable finalSolution;
    private List<TimeTableIteration> iterations;
}

