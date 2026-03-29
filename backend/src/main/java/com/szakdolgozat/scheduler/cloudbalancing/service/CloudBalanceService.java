package com.szakdolgozat.scheduler.cloudbalancing.service;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalanceIteration;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalanceSolutionWithIterations;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.domain.Computer;
import com.szakdolgozat.scheduler.cloudbalancing.solver.CloudBalancingConstraintProvider;
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
public class CloudBalanceService {

    private static final Logger log = LoggerFactory.getLogger(CloudBalanceService.class);

    private final SolverFactory<CloudBalance> solverFactory;
    private final ScoreManager<CloudBalance, HardSoftScore> scoreManager;

    public CloudBalanceService(
            @Qualifier("cloudBalanceSolverFactory") SolverFactory<CloudBalance> solverFactory,
            @Qualifier("cloudBalanceScoreManager") ScoreManager<CloudBalance, HardSoftScore> scoreManager) {
        this.solverFactory = solverFactory;
        this.scoreManager = scoreManager;
    }

    public CloudBalance solve(CloudBalance problem) {
        return solve(problem, 20);
    }

    public CloudBalance solve(CloudBalance problem, int timeLimit) {
        log.info("Starting cloud balance solve with {} processes, {} computers ({}s limit)",
                problem.getCloudProcessList().size(), problem.getComputerList().size(), timeLimit);
        Solver<CloudBalance> solver = buildSolver(timeLimit);
        CloudBalance solution = solver.solve(problem);
        log.info("Cloud balance solve complete. Score: {}", solution.getScore());
        return solution;
    }

    public CloudBalanceSolutionWithIterations solveWithIterations(CloudBalance problem) {
        return solveWithIterations(problem, 20);
    }

    public CloudBalanceSolutionWithIterations solveWithIterations(CloudBalance problem, int timeLimit) {
        log.info("Starting cloud balance solve (with iterations) with {} processes, {} computers ({}s limit)",
                problem.getCloudProcessList().size(), problem.getComputerList().size(), timeLimit);
        Solver<CloudBalance> solver = buildSolver(timeLimit);
        List<CloudBalanceIteration> iterations = new ArrayList<>();

        solver.addEventListener(event -> {
            if (event instanceof BestSolutionChangedEvent) {
                BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                CloudBalance solution = (CloudBalance) bestEvent.getNewBestSolution();

                CloudBalance solutionCopy = deepCopyCloudBalance(solution);

                boolean allAssigned = solution.getCloudProcessList().stream()
                        .allMatch(p -> p.getComputer() != null);
                String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";

                Map<String, String> constraintScores = extractConstraintScores(solutionCopy);

                CloudBalanceIteration iteration = new CloudBalanceIteration(
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

        CloudBalance finalSolution = solver.solve(problem);
        log.info("Cloud balance solve complete. Score: {}. Collected {} iterations",
                finalSolution.getScore(), iterations.size());
        return new CloudBalanceSolutionWithIterations(finalSolution, iterations);
    }

    public void solveWithStream(CloudBalance problem, int timeLimit, SseEmitter emitter) {
        log.info("Starting cloud balance SSE solve with {} processes ({}s limit)",
                problem.getCloudProcessList().size(), timeLimit);
        new Thread(() -> {
            try {
                Solver<CloudBalance> solver = buildSolver(timeLimit);
                List<CloudBalanceIteration> iterations = new ArrayList<>();

                solver.addEventListener(event -> {
                    if (event instanceof BestSolutionChangedEvent) {
                        BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                        CloudBalance solution = (CloudBalance) bestEvent.getNewBestSolution();
                        CloudBalance solutionCopy = deepCopyCloudBalance(solution);

                        boolean allAssigned = solution.getCloudProcessList().stream()
                                .allMatch(p -> p.getComputer() != null);
                        String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";
                        Map<String, String> constraintScores = extractConstraintScores(solutionCopy);

                        CloudBalanceIteration iteration = new CloudBalanceIteration(
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

                CloudBalance finalSolution = solver.solve(problem);
                log.info("Cloud balance SSE solve complete. Score: {}. {} iterations",
                        finalSolution.getScore(), iterations.size());
                emitter.send(SseEmitter.event().name("complete").data(
                        new CloudBalanceSolutionWithIterations(finalSolution, iterations)));
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE solve failed", e);
                emitter.completeWithError(e);
            }
        }, "cloudbalance-solver").start();
    }

    private Solver<CloudBalance> buildSolver(int timeLimit) {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(CloudBalance.class)
                .withEntityClasses(CloudProcess.class)
                .withConstraintProviderClass(CloudBalancingConstraintProvider.class)
                .withTerminationSpentLimit(java.time.Duration.ofSeconds(timeLimit));
        return SolverFactory.<CloudBalance>create(config).buildSolver();
    }

    private Map<String, String> extractConstraintScores(CloudBalance solution) {
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

    CloudBalance deepCopyCloudBalance(CloudBalance original) {
        CloudBalance copy = new CloudBalance(
                new ArrayList<>(),
                new ArrayList<>(original.getComputerList())
        );

        for (CloudProcess process : original.getCloudProcessList()) {
            CloudProcess processCopy = new CloudProcess(
                    process.getId(),
                    process.getRequiredCpuPower(),
                    process.getRequiredMemory(),
                    process.getRequiredBandwidth(),
                    process.getComputer()
            );
            copy.getCloudProcessList().add(processCopy);
        }

        return copy;
    }

    public CloudBalance generateDemoData(int numProcesses, int numComputers) {
        if (numProcesses <= 0) numProcesses = 6;
        if (numComputers <= 0) numComputers = 4;

        Random random = new Random(42);
        List<Computer> computerList = new ArrayList<>();
        for (int i = 0; i < numComputers; i++) {
            int scale = i + 1;
            computerList.add(new Computer(i, 4 * scale, 8 * scale, 10 * scale, 100 * scale));
        }

        List<CloudProcess> processList = new ArrayList<>();
        for (long i = 0; i < numProcesses; i++) {
            processList.add(new CloudProcess(i,
                    1 + random.nextInt(4),
                    2 + random.nextInt(5),
                    2 + random.nextInt(4),
                    null));
        }

        return new CloudBalance(processList, computerList);
    }

    public CloudBalance generateDemoData() {
        // 10 computers with varied, asymmetric capacities and costs
        // Cheap computers have tight capacity, expensive ones are bigger
        List<Computer> computerList = new ArrayList<>();
        int idc = 0;
        computerList.add(new Computer(idc++, 6,  8,  6,  40));   // cheap, tight
        computerList.add(new Computer(idc++, 4,  10, 8,  50));   // cheap, memory-heavy
        computerList.add(new Computer(idc++, 8,  6,  10, 60));   // cheap, CPU-heavy
        computerList.add(new Computer(idc++, 6,  8,  12, 70));   // mid, bandwidth-heavy
        computerList.add(new Computer(idc++, 10, 12, 8,  100));  // mid, balanced
        computerList.add(new Computer(idc++, 8,  14, 10, 110));  // mid, memory-heavy
        computerList.add(new Computer(idc++, 12, 10, 14, 140));  // expensive, balanced
        computerList.add(new Computer(idc++, 14, 16, 12, 180));  // expensive, big
        computerList.add(new Computer(idc++, 10, 18, 16, 200));  // expensive, memory+bw
        computerList.add(new Computer(idc++, 16, 12, 10, 220));  // expensive, CPU-heavy

        // 40 processes — enough to create a real bin-packing challenge
        // Total: CPU~102, Mem~118, BW~102
        // Total computer capacity: CPU=94, Mem=114, BW=106
        // Capacity is LESS than demand for CPU and memory — impossible to fit all!
        // This forces the solver to continuously explore which processes to assign where
        List<CloudProcess> processList = new ArrayList<>();
        long id = 0;
        processList.add(new CloudProcess(id++, 2, 3, 2, null));
        processList.add(new CloudProcess(id++, 3, 4, 3, null));
        processList.add(new CloudProcess(id++, 1, 2, 1, null));
        processList.add(new CloudProcess(id++, 4, 2, 4, null));
        processList.add(new CloudProcess(id++, 2, 3, 5, null));
        processList.add(new CloudProcess(id++, 3, 5, 2, null));
        processList.add(new CloudProcess(id++, 1, 2, 3, null));
        processList.add(new CloudProcess(id++, 4, 4, 4, null));
        processList.add(new CloudProcess(id++, 2, 3, 2, null));
        processList.add(new CloudProcess(id++, 3, 3, 4, null));
        processList.add(new CloudProcess(id++, 2, 4, 1, null));
        processList.add(new CloudProcess(id++, 3, 2, 3, null));
        processList.add(new CloudProcess(id++, 2, 3, 2, null));
        processList.add(new CloudProcess(id++, 4, 3, 3, null));
        processList.add(new CloudProcess(id++, 1, 4, 2, null));
        processList.add(new CloudProcess(id++, 3, 2, 4, null));
        processList.add(new CloudProcess(id++, 2, 5, 1, null));
        processList.add(new CloudProcess(id++, 4, 3, 3, null));
        processList.add(new CloudProcess(id++, 3, 2, 2, null));
        processList.add(new CloudProcess(id++, 2, 3, 3, null));
        processList.add(new CloudProcess(id++, 3, 4, 2, null));
        processList.add(new CloudProcess(id++, 2, 2, 4, null));
        processList.add(new CloudProcess(id++, 4, 3, 2, null));
        processList.add(new CloudProcess(id++, 1, 3, 3, null));
        processList.add(new CloudProcess(id++, 3, 4, 2, null));
        processList.add(new CloudProcess(id++, 2, 2, 3, null));
        processList.add(new CloudProcess(id++, 3, 3, 2, null));
        processList.add(new CloudProcess(id++, 2, 4, 3, null));
        processList.add(new CloudProcess(id++, 4, 2, 2, null));
        processList.add(new CloudProcess(id++, 3, 3, 4, null));
        processList.add(new CloudProcess(id++, 2, 2, 3, null));
        processList.add(new CloudProcess(id++, 3, 4, 2, null));
        processList.add(new CloudProcess(id++, 2, 3, 2, null));
        processList.add(new CloudProcess(id++, 1, 2, 3, null));
        processList.add(new CloudProcess(id++, 3, 3, 2, null));
        processList.add(new CloudProcess(id++, 2, 3, 4, null));
        processList.add(new CloudProcess(id++, 4, 2, 3, null));
        processList.add(new CloudProcess(id++, 2, 4, 2, null));
        processList.add(new CloudProcess(id++, 3, 3, 3, null));
        processList.add(new CloudProcess(id++, 2, 2, 2, null));

        return new CloudBalance(processList, computerList);
    }
}
