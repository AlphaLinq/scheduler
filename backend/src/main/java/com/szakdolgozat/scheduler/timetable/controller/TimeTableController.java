package com.szakdolgozat.scheduler.timetable.controller;

import com.szakdolgozat.scheduler.service.TimeTableService;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "http://localhost:5173") // Vite default port
public class TimeTableController {

    @Autowired
    private TimeTableService timeTableService;

    @GetMapping("/demo")
    public TimeTable solveDemoData() {
        TimeTable problem = timeTableService.generateDemoData();
        return timeTableService.solve(problem);
    }

    @PostMapping("/solve")
    public TimeTable solve(@RequestBody TimeTable problem) {
        return timeTableService.solve(problem);
    }
}
