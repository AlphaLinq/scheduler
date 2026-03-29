package com.szakdolgozat.scheduler.cloudbalancing.service;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalanceSolutionWithIterations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CloudBalanceServiceTest {

    @Autowired
    private CloudBalanceService cloudBalanceService;

    @Test
    void testGenerateDemoDataValidity() {
        CloudBalance demo = cloudBalanceService.generateDemoData();
        assertEquals(10, demo.getComputerList().size());
        assertEquals(40, demo.getCloudProcessList().size());
        demo.getCloudProcessList().forEach(process -> {
            assertNotNull(process.getId());
            assertTrue(process.getRequiredCpuPower() > 0);
            assertTrue(process.getRequiredMemory() > 0);
            assertTrue(process.getRequiredBandwidth() > 0);
        });
    }

    @Test
    void testSolveReturnsScore() {
        CloudBalance problem = cloudBalanceService.generateDemoData();
        CloudBalance solution = cloudBalanceService.solve(problem);
        assertNotNull(solution);
        assertNotNull(solution.getScore());
        // Demo data is intentionally over-subscribed (demand > capacity) to create
        // a challenging problem, so feasibility is not guaranteed
    }

    @Test
    void testSolveWithIterationsCollectsIterations() {
        CloudBalance problem = cloudBalanceService.generateDemoData();
        CloudBalanceSolutionWithIterations result = cloudBalanceService.solveWithIterations(problem);
        assertNotNull(result.getFinalSolution());
        assertFalse(result.getIterations().isEmpty(), "Should have collected at least one iteration");
        result.getIterations().forEach(iter -> {
            assertNotNull(iter.getScore());
            assertTrue(iter.getTimeMillis() >= 0);
            assertTrue(iter.getStepCount() > 0);
            assertNotNull(iter.getSolution());
        });
    }

    @Test
    void testDeepCopyIntegrity() {
        CloudBalance problem = cloudBalanceService.generateDemoData();
        CloudBalanceSolutionWithIterations result = cloudBalanceService.solveWithIterations(problem);
        if (result.getIterations().size() >= 2) {
            var iter0 = result.getIterations().get(0).getSolution();
            var iter1 = result.getIterations().get(1).getSolution();
            var process0 = iter0.getCloudProcessList().get(0);
            var originalComputer1 = iter1.getCloudProcessList().get(0).getComputer();
            process0.setComputer(null);
            assertEquals(originalComputer1, iter1.getCloudProcessList().get(0).getComputer(),
                    "Deep copy should isolate iterations from each other");
        }
    }
}
