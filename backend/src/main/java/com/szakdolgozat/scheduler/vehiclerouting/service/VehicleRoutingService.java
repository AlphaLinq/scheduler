package com.szakdolgozat.scheduler.vehiclerouting.service;

import com.szakdolgozat.scheduler.vehiclerouting.domain.Customer;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Location;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Vehicle;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingIteration;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolutionWithIterations;
import com.szakdolgozat.scheduler.vehiclerouting.solver.VehicleRoutingConstraintProvider;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class VehicleRoutingService {

    private static final Logger log = LoggerFactory.getLogger(VehicleRoutingService.class);

    private final SolverFactory<VehicleRoutingSolution> solverFactory;
    private final ScoreManager<VehicleRoutingSolution, HardSoftScore> scoreManager;

    public VehicleRoutingService(
            @Qualifier("vehicleRoutingSolverFactory") SolverFactory<VehicleRoutingSolution> solverFactory,
            @Qualifier("vehicleRoutingScoreManager") ScoreManager<VehicleRoutingSolution, HardSoftScore> scoreManager) {
        this.solverFactory = solverFactory;
        this.scoreManager = scoreManager;
    }

    public VehicleRoutingSolution solve(VehicleRoutingSolution problem) {
        return solve(problem, 30);
    }

    public VehicleRoutingSolution solve(VehicleRoutingSolution problem, int timeLimit) {
        log.info("Starting vehicle routing solve with {} customers, {} vehicles ({}s limit)",
                problem.getCustomerList().size(), problem.getVehicleList().size(), timeLimit);
        Solver<VehicleRoutingSolution> solver = buildSolver(timeLimit);
        VehicleRoutingSolution solution = solver.solve(problem);
        log.info("Vehicle routing solve complete. Score: {}", solution.getScore());
        return solution;
    }

    public VehicleRoutingSolutionWithIterations solveWithIterations(VehicleRoutingSolution problem) {
        return solveWithIterations(problem, 30);
    }

    public VehicleRoutingSolutionWithIterations solveWithIterations(VehicleRoutingSolution problem, int timeLimit) {
        log.info("Starting vehicle routing solve (with iterations) with {} customers, {} vehicles ({}s limit)",
                problem.getCustomerList().size(), problem.getVehicleList().size(), timeLimit);
        Solver<VehicleRoutingSolution> solver = buildSolver(timeLimit);
        List<VehicleRoutingIteration> iterations = new ArrayList<>();

        solver.addEventListener(event -> {
            if (event instanceof BestSolutionChangedEvent) {
                BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                VehicleRoutingSolution solution = (VehicleRoutingSolution) bestEvent.getNewBestSolution();

                VehicleRoutingSolution solutionCopy = deepCopyVehicleRoutingSolution(solution);

                boolean allAssigned = solution.getCustomerList().stream()
                        .allMatch(c -> c.getVehicle() != null);
                String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";

                Map<String, String> constraintScores = extractConstraintScores(solutionCopy);

                VehicleRoutingIteration iteration = new VehicleRoutingIteration(
                        iterations.size() + 1,
                        bestEvent.getTimeMillisSpent(),
                        solution.getScore(),
                        solutionCopy,
                        phaseName,
                        constraintScores
                );
                iterations.add(iteration);
                log.debug("New best solution at step {}, time {}ms, score: {}, phase: {}",
                        iteration.getStepCount(), iteration.getTimeMillis(), iteration.getScore(), phaseName);
            }
        });

        VehicleRoutingSolution finalSolution = solver.solve(problem);
        log.info("Vehicle routing solve complete. Score: {}. Collected {} iterations",
                finalSolution.getScore(), iterations.size());
        return new VehicleRoutingSolutionWithIterations(finalSolution, iterations);
    }

    public void solveWithStream(VehicleRoutingSolution problem, int timeLimit, SseEmitter emitter) {
        log.info("Starting vehicle routing SSE solve with {} customers ({}s limit)",
                problem.getCustomerList().size(), timeLimit);
        new Thread(() -> {
            try {
                Solver<VehicleRoutingSolution> solver = buildSolver(timeLimit);
                List<VehicleRoutingIteration> iterations = new ArrayList<>();

                solver.addEventListener(event -> {
                    if (event instanceof BestSolutionChangedEvent) {
                        BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                        VehicleRoutingSolution solution = (VehicleRoutingSolution) bestEvent.getNewBestSolution();
                        VehicleRoutingSolution solutionCopy = deepCopyVehicleRoutingSolution(solution);

                        boolean allAssigned = solution.getCustomerList().stream()
                                .allMatch(c -> c.getVehicle() != null);
                        String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";
                        Map<String, String> constraintScores = extractConstraintScores(solutionCopy);

                        VehicleRoutingIteration iteration = new VehicleRoutingIteration(
                                iterations.size() + 1, bestEvent.getTimeMillisSpent(),
                                solution.getScore(), solutionCopy, phaseName, constraintScores);
                        iterations.add(iteration);

                        try {
                            emitter.send(SseEmitter.event().name("iteration").data(iteration));
                        } catch (Exception e) {
                            log.debug("SSE send failed: {}", e.getMessage());
                        }
                    }
                });

                VehicleRoutingSolution finalSolution = solver.solve(problem);
                log.info("Vehicle routing SSE solve complete. Score: {}. {} iterations",
                        finalSolution.getScore(), iterations.size());
                emitter.send(SseEmitter.event().name("complete").data(
                        new VehicleRoutingSolutionWithIterations(finalSolution, iterations)));
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE solve failed", e);
                emitter.completeWithError(e);
            }
        }, "vehiclerouting-solver").start();
    }

    private Solver<VehicleRoutingSolution> buildSolver(int timeLimit) {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Customer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationSpentLimit(java.time.Duration.ofSeconds(timeLimit));
        return SolverFactory.<VehicleRoutingSolution>create(config).buildSolver();
    }

    private Map<String, String> extractConstraintScores(VehicleRoutingSolution solution) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            scoreManager.explainScore(solution).getConstraintMatchTotalMap()
                    .forEach((constraintId, matchTotal) -> {
                        String name = constraintId.contains("/") ? constraintId.substring(constraintId.lastIndexOf('/') + 1) : constraintId;
                        result.put(name, matchTotal.getScore().toString());
                    });
        } catch (Exception e) {
            log.debug("Could not explain score: {}", e.getMessage());
        }
        return result;
    }

    VehicleRoutingSolution deepCopyVehicleRoutingSolution(VehicleRoutingSolution original) {
        VehicleRoutingSolution copy = new VehicleRoutingSolution(
                new ArrayList<>(),
                new ArrayList<>(original.getVehicleList()),
                new ArrayList<>(original.getLocationList())
        );

        for (Customer customer : original.getCustomerList()) {
            Customer customerCopy = new Customer(
                    customer.getId(),
                    customer.getName(),
                    customer.getLocation(),
                    customer.getDemand(),
                    customer.getServiceDuration(),
                    customer.getReadyTime(),
                    customer.getDueTime()
            );
            customerCopy.setVehicle(customer.getVehicle());
            copy.getCustomerList().add(customerCopy);
        }

        return copy;
    }

    public VehicleRoutingSolution generateDemoData(int numCustomers, int numVehicles) {
        if (numCustomers <= 0) numCustomers = 8;
        if (numVehicles <= 0) numVehicles = 3;

        Random random = new Random(42);
        Location depot = new Location(0L, "Depot", 47.4979, 19.0402);

        List<Location> locationList = new ArrayList<>();
        locationList.add(depot);
        for (int i = 1; i <= numCustomers; i++) {
            locationList.add(new Location((long) i, "Customer " + i,
                    47.48 + random.nextDouble() * 0.06,
                    19.02 + random.nextDouble() * 0.06));
        }

        List<Vehicle> vehicleList = new ArrayList<>();
        for (int i = 0; i < numVehicles; i++) {
            vehicleList.add(new Vehicle((long) i, "Vehicle " + (i + 1), 15 + (i * 5), depot));
        }

        List<Customer> customerList = new ArrayList<>();
        for (int i = 0; i < numCustomers; i++) {
            int readyTime = random.nextInt(120);
            int dueTime = readyTime + 60 + random.nextInt(120);
            customerList.add(new Customer((long) i, "Customer " + (i + 1), locationList.get(i + 1),
                    2 + random.nextInt(5), 10 + random.nextInt(20), readyTime, dueTime));
        }

        return new VehicleRoutingSolution(customerList, vehicleList, locationList);
    }

    public VehicleRoutingSolution generateDemoData() {
        // Depot in Budapest
        Location depot = new Location(0L, "Depot (Budapest)", 47.50, 19.04);

        // 20 customers spread across Hungary — distances 20-200km from depot
        // This creates meaningful travel times (20-200 min) that interact with time windows
        List<Location> locationList = new ArrayList<>();
        locationList.add(depot);
        locationList.add(new Location(1L,  "Szekesfehervar",  47.19, 18.41));  // ~50km SW
        locationList.add(new Location(2L,  "Eger",            47.90, 20.37));  // ~120km NE
        locationList.add(new Location(3L,  "Gyor",            47.68, 17.63));  // ~120km W
        locationList.add(new Location(4L,  "Kecskemet",       46.91, 19.69));  // ~85km SE
        locationList.add(new Location(5L,  "Veszprem",        47.09, 17.91));  // ~100km SW
        locationList.add(new Location(6L,  "Szentendre",      47.67, 19.08));  // ~20km N
        locationList.add(new Location(7L,  "Vac",             47.78, 19.13));  // ~30km N
        locationList.add(new Location(8L,  "Dunaujvaros",     46.98, 18.93));  // ~60km S
        locationList.add(new Location(9L,  "Hatvan",          47.67, 19.68));  // ~60km NE
        locationList.add(new Location(10L, "Tatabanya",       47.57, 18.39));  // ~60km W
        locationList.add(new Location(11L, "Cegled",          47.17, 19.80));  // ~75km SE
        locationList.add(new Location(12L, "Gyongyos",        47.78, 19.93));  // ~80km NE
        locationList.add(new Location(13L, "Erd",             47.39, 18.91));  // ~15km S
        locationList.add(new Location(14L, "Szolnok",         47.17, 20.18));  // ~115km SE
        locationList.add(new Location(15L, "Siofok",          46.91, 18.05));  // ~110km SW
        locationList.add(new Location(16L, "Monor",           47.35, 19.45));  // ~35km SE
        locationList.add(new Location(17L, "Godollo",         47.60, 19.35));  // ~30km NE
        locationList.add(new Location(18L, "Esztergom",       47.79, 18.74));  // ~55km NW
        locationList.add(new Location(19L, "Dunakeszi",       47.63, 19.14));  // ~15km N
        locationList.add(new Location(20L, "Dabas",           47.19, 19.31));  // ~40km S

        // 4 vehicles with tight capacity — total capacity 56, total demand ~63
        List<Vehicle> vehicleList = new ArrayList<>();
        vehicleList.add(new Vehicle(0L, "Vehicle 1", 14, depot));
        vehicleList.add(new Vehicle(1L, "Vehicle 2", 14, depot));
        vehicleList.add(new Vehicle(2L, "Vehicle 3", 14, depot));
        vehicleList.add(new Vehicle(3L, "Vehicle 4", 14, depot));

        // 20 customers — tight time windows that interact with real travel times
        // Travel time ≈ distance in km (at 60km/h), so 50km = 50min, 120km = 120min
        // Some time windows are deliberately tight: due time close to travel time
        List<Customer> customerList = new ArrayList<>();
        customerList.add(new Customer(0L,  "Szekesfehervar",  locationList.get(1),  4, 20, 30,   70));  // ~50km, due 70 — tight
        customerList.add(new Customer(1L,  "Eger",            locationList.get(2),  3, 15, 60,  150));  // ~120km, due 150 — tight
        customerList.add(new Customer(2L,  "Gyor",            locationList.get(3),  5, 25, 80,  140));  // ~120km, due 140 — tight!
        customerList.add(new Customer(3L,  "Kecskemet",       locationList.get(4),  3, 15, 50,  110));  // ~85km, due 110 — tight
        customerList.add(new Customer(4L,  "Veszprem",        locationList.get(5),  4, 20, 60,  120));  // ~100km, due 120 — tight!
        customerList.add(new Customer(5L,  "Szentendre",      locationList.get(6),  2, 10,  0,   40));  // ~20km, due 40 — tight
        customerList.add(new Customer(6L,  "Vac",             locationList.get(7),  3, 15, 10,   50));  // ~30km, due 50 — ok
        customerList.add(new Customer(7L,  "Dunaujvaros",     locationList.get(8),  4, 20, 30,   80));  // ~60km, due 80 — tight
        customerList.add(new Customer(8L,  "Hatvan",          locationList.get(9),  2, 10, 20,   80));  // ~60km, due 80 — ok
        customerList.add(new Customer(9L,  "Tatabanya",       locationList.get(10), 3, 15, 30,   80));  // ~60km, due 80 — tight
        customerList.add(new Customer(10L, "Cegled",          locationList.get(11), 4, 20, 40,   90));  // ~75km, due 90 — tight
        customerList.add(new Customer(11L, "Gyongyos",        locationList.get(12), 2, 10, 50,  100));  // ~80km, due 100 — tight
        customerList.add(new Customer(12L, "Erd",             locationList.get(13), 3, 10,  0,   30));  // ~15km, due 30 — tight!
        customerList.add(new Customer(13L, "Szolnok",         locationList.get(14), 5, 25, 70,  130));  // ~115km, due 130 — tight!
        customerList.add(new Customer(14L, "Siofok",          locationList.get(15), 3, 15, 60,  130));  // ~110km, due 130 — tight
        customerList.add(new Customer(15L, "Monor",           locationList.get(16), 2, 10, 10,   50));  // ~35km, due 50 — ok
        customerList.add(new Customer(16L, "Godollo",         locationList.get(17), 3, 15, 10,   50));  // ~30km, due 50 — ok
        customerList.add(new Customer(17L, "Esztergom",       locationList.get(18), 3, 15, 20,   70));  // ~55km, due 70 — tight
        customerList.add(new Customer(18L, "Dunakeszi",       locationList.get(19), 2, 10,  0,   30));  // ~15km, due 30 — tight!
        customerList.add(new Customer(19L, "Dabas",           locationList.get(20), 3, 15, 20,   55));  // ~40km, due 55 — tight

        return new VehicleRoutingSolution(customerList, vehicleList, locationList);
    }
}
