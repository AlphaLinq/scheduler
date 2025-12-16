package com.szakdolgozat.scheduler.timetable.controller;

import com.szakdolgozat.scheduler.timetable.service.TimeTableService;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import com.szakdolgozat.scheduler.timetable.domain.TimeTableSolutionWithIterations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetable")
public class TimeTableController {

    @Autowired
    private TimeTableService timeTableService;

    @GetMapping("/demo")
    public TimeTable solveDemoData() {
        TimeTable problem = timeTableService.generateDemoData();
        return timeTableService.solve(problem);
    }

    @GetMapping("/demo/iterations")
    public TimeTableSolutionWithIterations solveDemoDataWithIterations() {
        TimeTable problem = timeTableService.generateDemoData();
        return timeTableService.solveWithIterations(problem);
    }

    @PostMapping("/solve")
    public TimeTable solve(@RequestBody TimeTable problem) {
        return timeTableService.solve(problem);
    }

    @PostMapping("/solve/iterations")
    public TimeTableSolutionWithIterations solveWithIterations(@RequestBody TimeTable problem) {
        return timeTableService.solveWithIterations(problem);
    }
}
