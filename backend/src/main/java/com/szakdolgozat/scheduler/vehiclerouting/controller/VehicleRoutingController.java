package com.szakdolgozat.scheduler.vehiclerouting.controller;

import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import com.szakdolgozat.scheduler.vehiclerouting.service.VehicleRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/vehiclerouting")
@CrossOrigin("http://localhost:5173")
public class VehicleRoutingController {

    @Autowired
    private VehicleRoutingService vehicleRoutingService;

    @GetMapping("/demo")
    public VehicleRoutingSolution solveDemoData() {
        VehicleRoutingSolution problem = vehicleRoutingService.generateDemoData();
        return vehicleRoutingService.solve(problem);
    }
}
