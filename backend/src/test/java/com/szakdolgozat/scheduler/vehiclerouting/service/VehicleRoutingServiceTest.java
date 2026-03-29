package com.szakdolgozat.scheduler.vehiclerouting.service;

import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolutionWithIterations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VehicleRoutingServiceTest {

    @Autowired
    private VehicleRoutingService vehicleRoutingService;

    @Test
    public void testSolve() {
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData();
        VehicleRoutingSolution solution = vehicleRoutingService.solve(problem);
        assertNotNull(solution);
        assertNotNull(solution.getScore());
    }

    @Test
    void testGenerateDemoDataValidity() {
        VehicleRoutingSolution demo = vehicleRoutingService.generateDemoData();
        assertEquals(4, demo.getVehicleList().size());
        assertEquals(20, demo.getCustomerList().size());
        assertEquals(21, demo.getLocationList().size());
        demo.getCustomerList().forEach(customer -> {
            assertNotNull(customer.getId());
            assertNotNull(customer.getName());
            assertNotNull(customer.getLocation());
            assertTrue(customer.getDemand() > 0);
        });
    }

    @Test
    void testSolveWithIterationsCollectsIterations() {
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData();
        VehicleRoutingSolutionWithIterations result = vehicleRoutingService.solveWithIterations(problem);
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
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData();
        VehicleRoutingSolutionWithIterations result = vehicleRoutingService.solveWithIterations(problem);
        if (result.getIterations().size() >= 2) {
            var iter0 = result.getIterations().get(0).getSolution();
            var iter1 = result.getIterations().get(1).getSolution();
            var customer0 = iter0.getCustomerList().get(0);
            var originalVehicle1 = iter1.getCustomerList().get(0).getVehicle();
            customer0.setVehicle(null);
            assertEquals(originalVehicle1, iter1.getCustomerList().get(0).getVehicle(),
                    "Deep copy should isolate iterations from each other");
        }
    }
}
