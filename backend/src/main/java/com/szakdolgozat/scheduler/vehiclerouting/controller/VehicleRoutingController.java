package com.szakdolgozat.scheduler.vehiclerouting.controller;

import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolutionWithIterations;
import com.szakdolgozat.scheduler.vehiclerouting.service.VehicleRoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("api/vehiclerouting")
@Tag(name = "Vehicle Routing", description = "Vehicle routing (CVRPTW) endpoints")
public class VehicleRoutingController {

    @Autowired
    private VehicleRoutingService vehicleRoutingService;

    @GetMapping("/demo")
    @Operation(summary = "Solve demo vehicle routing", description = "Generates demo data and returns the final solution")
    public VehicleRoutingSolution solveDemoData(
            @Parameter(description = "Number of customers") @RequestParam(defaultValue = "20") int customers,
            @Parameter(description = "Number of vehicles") @RequestParam(defaultValue = "4") int vehicles,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData(customers, vehicles);
        return vehicleRoutingService.solve(problem, timeLimit);
    }

    @GetMapping("/demo/iterations")
    @Operation(summary = "Solve demo vehicle routing with iterations", description = "Generates demo data and returns the solution with iteration history")
    public VehicleRoutingSolutionWithIterations solveDemoDataWithIterations(
            @Parameter(description = "Number of customers") @RequestParam(defaultValue = "20") int customers,
            @Parameter(description = "Number of vehicles") @RequestParam(defaultValue = "4") int vehicles,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData(customers, vehicles);
        return vehicleRoutingService.solveWithIterations(problem, timeLimit);
    }

    @GetMapping(value = "/demo/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream demo vehicle routing solving", description = "Streams iterations as SSE events in real-time")
    public SseEmitter streamDemoSolve(
            @Parameter(description = "Number of customers") @RequestParam(defaultValue = "20") int customers,
            @Parameter(description = "Number of vehicles") @RequestParam(defaultValue = "4") int vehicles,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        SseEmitter emitter = new SseEmitter(120_000L);
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData(customers, vehicles);
        vehicleRoutingService.solveWithStream(problem, timeLimit, emitter);
        return emitter;
    }

    @PostMapping("/solve")
    @Operation(summary = "Solve custom vehicle routing", description = "Solves a user-provided vehicle routing problem")
    public VehicleRoutingSolution solve(
            @Valid @RequestBody VehicleRoutingSolution problem,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        return vehicleRoutingService.solve(problem, timeLimit);
    }

    @PostMapping("/solve/iterations")
    @Operation(summary = "Solve custom vehicle routing with iterations")
    public VehicleRoutingSolutionWithIterations solveWithIterations(
            @Valid @RequestBody VehicleRoutingSolution problem,
            @Parameter(description = "Solver time limit in seconds") @RequestParam(defaultValue = "30") int timeLimit) {
        return vehicleRoutingService.solveWithIterations(problem, timeLimit);
    }
}
