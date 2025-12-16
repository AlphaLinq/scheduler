package com.szakdolgozat.scheduler.vehiclerouting.service;

import com.szakdolgozat.scheduler.vehiclerouting.domain.Customer;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Location;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Vehicle;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleRoutingService {

    private final SolverFactory<VehicleRoutingSolution> solverFactory;

    public VehicleRoutingService(@Qualifier("vehicleRoutingSolverFactory") SolverFactory<VehicleRoutingSolution> solverFactory) {
        this.solverFactory = solverFactory;
    }

    public VehicleRoutingSolution solve(VehicleRoutingSolution problem) {
        Solver<VehicleRoutingSolution> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }

    public VehicleRoutingSolution generateDemoData() {
        // Create depot location
        Location depot = new Location(0L, "Depot", 47.4979, 19.0402); // Budapest coordinates

        // Create customer locations
        List<Location> locationList = new ArrayList<>();
        locationList.add(depot);
        locationList.add(new Location(1L, "Customer 1", 47.5100, 19.0500));
        locationList.add(new Location(2L, "Customer 2", 47.5200, 19.0300));
        locationList.add(new Location(3L, "Customer 3", 47.4900, 19.0600));
        locationList.add(new Location(4L, "Customer 4", 47.4800, 19.0200));
        locationList.add(new Location(5L, "Customer 5", 47.5050, 19.0450));
        locationList.add(new Location(6L, "Customer 6", 47.4950, 19.0350));
        locationList.add(new Location(7L, "Customer 7", 47.5150, 19.0250));
        locationList.add(new Location(8L, "Customer 8", 47.4850, 19.0550));

        // Create vehicles
        List<Vehicle> vehicleList = new ArrayList<>();
        vehicleList.add(new Vehicle(0L, "Vehicle 1", 15, depot));
        vehicleList.add(new Vehicle(1L, "Vehicle 2", 15, depot));
        vehicleList.add(new Vehicle(2L, "Vehicle 3", 20, depot));

        // Create customers with demands and time windows (CVRPTW)
        // Parameters: id, name, location, demand, serviceDuration, readyTime, dueTime
        List<Customer> customerList = new ArrayList<>();
        customerList.add(new Customer(0L, "Customer 1", locationList.get(1), 3, 15, 60, 180));
        customerList.add(new Customer(1L, "Customer 2", locationList.get(2), 4, 20, 30, 150));
        customerList.add(new Customer(2L, "Customer 3", locationList.get(3), 2, 10, 90, 200));
        customerList.add(new Customer(3L, "Customer 4", locationList.get(4), 5, 25, 45, 160));
        customerList.add(new Customer(4L, "Customer 5", locationList.get(5), 3, 15, 0, 120));
        customerList.add(new Customer(5L, "Customer 6", locationList.get(6), 4, 20, 75, 190));
        customerList.add(new Customer(6L, "Customer 7", locationList.get(7), 6, 30, 100, 220));
        customerList.add(new Customer(7L, "Customer 8", locationList.get(8), 3, 15, 50, 170));

        return new VehicleRoutingSolution(customerList, vehicleList, locationList);
    }
}

