package com.szakdolgozat.scheduler.timetable.controller;

import com.szakdolgozat.scheduler.timetable.service.TimeTableService;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import com.szakdolgozat.scheduler.timetable.domain.TimeTableSolutionWithIterations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/timetable")
@Tag(name = "Timetable", description = "Timetable scheduling endpoints")
public class TimeTableController {

    @Autowired
    private TimeTableService timeTableService;

    @GetMapping("/demo")
    @Operation(summary = "Solve demo timetable", description = "Generates demo data and returns the final solution")
    public TimeTable solveDemoData(
            @Parameter(description = "Number of lessons") @RequestParam(defaultValue = "20") int lessons,
            @Parameter(description = "Number of rooms") @RequestParam(defaultValue = "3") int rooms,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        TimeTable problem = timeTableService.generateDemoData(lessons, rooms);
        return timeTableService.solve(problem, timeLimit);
    }

    @GetMapping("/demo/iterations")
    @Operation(summary = "Solve demo timetable with iterations", description = "Generates demo data and returns the solution with iteration history")
    public TimeTableSolutionWithIterations solveDemoDataWithIterations(
            @Parameter(description = "Number of lessons") @RequestParam(defaultValue = "20") int lessons,
            @Parameter(description = "Number of rooms") @RequestParam(defaultValue = "3") int rooms,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        TimeTable problem = timeTableService.generateDemoData(lessons, rooms);
        return timeTableService.solveWithIterations(problem, timeLimit);
    }

    @GetMapping(value = "/demo/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream demo timetable solving", description = "Streams iterations as SSE events in real-time")
    public SseEmitter streamDemoSolve(
            @Parameter(description = "Number of lessons") @RequestParam(defaultValue = "20") int lessons,
            @Parameter(description = "Number of rooms") @RequestParam(defaultValue = "3") int rooms,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        SseEmitter emitter = new SseEmitter(120_000L);
        TimeTable problem = timeTableService.generateDemoData(lessons, rooms);
        timeTableService.solveWithStream(problem, timeLimit, emitter);
        return emitter;
    }

    @PostMapping("/solve")
    @Operation(summary = "Solve custom timetable", description = "Solves a user-provided timetable problem")
    public TimeTable solve(
            @Valid @RequestBody TimeTable problem,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        return timeTableService.solve(problem, timeLimit);
    }

    @PostMapping("/solve/iterations")
    @Operation(summary = "Solve custom timetable with iterations", description = "Solves a user-provided timetable problem with iteration history")
    public TimeTableSolutionWithIterations solveWithIterations(
            @Valid @RequestBody TimeTable problem,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        return timeTableService.solveWithIterations(problem, timeLimit);
    }
}
