package com.szakdolgozat.scheduler.cloudbalancing.controller;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalanceSolutionWithIterations;
import com.szakdolgozat.scheduler.cloudbalancing.service.CloudBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("api/cloudbalance")
@Tag(name = "Cloud Balance", description = "Cloud balancing endpoints")
public class CloudBalanceController {

    @Autowired
    private CloudBalanceService cloudBalanceService;

    @GetMapping("/demo")
    @Operation(summary = "Solve demo cloud balance", description = "Generates demo data and returns the final solution")
    public CloudBalance solveDemoData(
            @Parameter(description = "Number of processes") @RequestParam(defaultValue = "40") int processes,
            @Parameter(description = "Number of computers") @RequestParam(defaultValue = "10") int computers,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "20") int timeLimit) {
        CloudBalance problem = cloudBalanceService.generateDemoData(processes, computers);
        return cloudBalanceService.solve(problem, timeLimit);
    }

    @GetMapping("/demo/iterations")
    @Operation(summary = "Solve demo cloud balance with iterations", description = "Generates demo data and returns the solution with iteration history")
    public CloudBalanceSolutionWithIterations solveDemoDataWithIterations(
            @Parameter(description = "Number of processes") @RequestParam(defaultValue = "40") int processes,
            @Parameter(description = "Number of computers") @RequestParam(defaultValue = "10") int computers,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "20") int timeLimit) {
        CloudBalance problem = cloudBalanceService.generateDemoData(processes, computers);
        return cloudBalanceService.solveWithIterations(problem, timeLimit);
    }

    @GetMapping(value = "/demo/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream demo cloud balance solving", description = "Streams iterations as SSE events in real-time")
    public SseEmitter streamDemoSolve(
            @Parameter(description = "Number of processes") @RequestParam(defaultValue = "40") int processes,
            @Parameter(description = "Number of computers") @RequestParam(defaultValue = "10") int computers,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "20") int timeLimit) {
        SseEmitter emitter = new SseEmitter(120_000L);
        CloudBalance problem = cloudBalanceService.generateDemoData(processes, computers);
        cloudBalanceService.solveWithStream(problem, timeLimit, emitter);
        return emitter;
    }

    @PostMapping("/solve")
    @Operation(summary = "Solve custom cloud balance", description = "Solves a user-provided cloud balance problem")
    public CloudBalance solve(
            @Valid @RequestBody CloudBalance problem,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "20") int timeLimit) {
        return cloudBalanceService.solve(problem, timeLimit);
    }

    @PostMapping("/solve/iterations")
    @Operation(summary = "Solve custom cloud balance with iterations")
    public CloudBalanceSolutionWithIterations solveWithIterations(
            @Valid @RequestBody CloudBalance problem,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "20") int timeLimit) {
        return cloudBalanceService.solveWithIterations(problem, timeLimit);
    }
}
