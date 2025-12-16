package com.szakdolgozat.scheduler.vehiclerouting.solver;

import com.szakdolgozat.scheduler.vehiclerouting.domain.Customer;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                vehicleCapacity(constraintFactory),
                customerDueTime(constraintFactory),
                // Soft constraints
                minimizeTotalDistance(constraintFactory)
        };
    }

    // Hard constraint: vehicle capacity cannot be exceeded
    private Constraint vehicleCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Customer.class)
                .groupBy(Customer::getVehicle, sum(Customer::getDemand))
                .filter((vehicle, totalDemand) -> vehicle != null && totalDemand > vehicle.getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        (vehicle, totalDemand) -> totalDemand - vehicle.getCapacity())
                .asConstraint("Vehicle capacity");
    }

    // Hard constraint: customer ready time - vehicle cannot arrive before ready time (but can wait)
    // Hard constraint: customer due time - vehicle must arrive before due time
    private Constraint customerDueTime(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Customer.class)
                .filter(customer -> customer.getVehicle() != null && customer.getDueTime() != null)
                .penalize(HardSoftScore.ONE_HARD,
                        customer -> {
                            int travelTime = customer.getTravelTimeFromDepot();
                            int arrivalTime = travelTime;
                            int lateness = arrivalTime - customer.getDueTime();
                            return Math.max(0, lateness); // Penalize if late
                        })
                .asConstraint("Customer due time");
    }

    // Soft constraint: minimize total distance traveled by all vehicles (fuel consumption)
    private Constraint minimizeTotalDistance(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Customer.class)
                .filter(customer -> customer.getVehicle() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        customer -> (int) (customer.getDistanceFromDepot() * 10))
                .asConstraint("Minimize total distance");
    }
}

