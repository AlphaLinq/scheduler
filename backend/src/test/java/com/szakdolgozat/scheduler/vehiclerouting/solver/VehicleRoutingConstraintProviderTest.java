package com.szakdolgozat.scheduler.vehiclerouting.solver;

import com.szakdolgozat.scheduler.vehiclerouting.domain.Customer;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Location;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Vehicle;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleRoutingConstraintProviderTest {

    private final ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutingSolution> constraintVerifier =
            ConstraintVerifier.build(new VehicleRoutingConstraintProvider(), VehicleRoutingSolution.class, Customer.class);

    private final Location depot = new Location(0L, "Depot", 0.0, 0.0);

    @Test
    void vehicleCapacity_violated() {
        Vehicle vehicle = new Vehicle(0L, "V1", 10, depot);
        Location nearLoc = new Location(1L, "L1", 0.001, 0.0);
        Customer c1 = new Customer(0L, "C1", nearLoc, 6, 15, null, null);
        c1.setVehicle(vehicle);
        Customer c2 = new Customer(1L, "C2", nearLoc, 6, 15, null, null);
        c2.setVehicle(vehicle);
        // demand = 12 > capacity 10, overage = 2
        // No due time => only capacity hard constraint + distance soft

        constraintVerifier.verifyThat()
                .given(vehicle, c1, c2)
                .scores(HardSoftScore.of(-2, -2));
        // soft = 2 * (int)(0.001*111*10) = 2*1 = -2 approximately
    }

    @Test
    void vehicleCapacity_satisfied_noDueTime() {
        Vehicle vehicle = new Vehicle(0L, "V1", 15, depot);
        Location nearLoc = new Location(1L, "L1", 0.001, 0.0);
        Customer c1 = new Customer(0L, "C1", nearLoc, 6, 15, null, null);
        c1.setVehicle(vehicle);
        Customer c2 = new Customer(1L, "C2", nearLoc, 6, 15, null, null);
        c2.setVehicle(vehicle);
        // demand = 12 <= 15, no hard penalty

        constraintVerifier.verifyThat()
                .given(vehicle, c1, c2)
                .scores(HardSoftScore.of(0, -2));
    }

    @Test
    void minimizeTotalDistance() {
        Vehicle vehicle = new Vehicle(0L, "V1", 20, depot);
        Location loc = new Location(1L, "L1", 1.0, 0.0);
        Customer customer = new Customer(0L, "C1", loc, 3, 15, null, null);
        customer.setVehicle(vehicle);
        // distance = sqrt(1)*111 = 111km, soft penalty = (int)(111*10) = 1110
        // No due time => hard = 0

        constraintVerifier.verifyThat()
                .given(vehicle, customer)
                .scores(HardSoftScore.of(0, -1110));
    }

    @Test
    void customerDueTime_late() {
        Vehicle vehicle = new Vehicle(0L, "V1", 20, depot);
        Location farLoc = new Location(1L, "Far", 1.0, 0.0);
        Customer customer = new Customer(0L, "C1", farLoc, 3, 15, 0, 50);
        customer.setVehicle(vehicle);
        // travelTime = (int)(111 * 60.0 / 60.0) = 111 min
        // lateness = 111 - 50 = 61 => hard = -61
        // soft = -1110

        constraintVerifier.verifyThat()
                .given(vehicle, customer)
                .scores(HardSoftScore.of(-61, -1110));
    }
}
