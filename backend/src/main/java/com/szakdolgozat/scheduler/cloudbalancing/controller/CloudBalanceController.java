package com.szakdolgozat.scheduler.cloudbalancing.controller;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.service.CloudBalanceService;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cloudbalance")
public class CloudBalanceController {

    @Autowired
    private CloudBalanceService  cloudBalanceService;

    @GetMapping("/demo")
    public CloudBalance solveDemoData() {
        CloudBalance problem = cloudBalanceService.generateDemoData();
        return cloudBalanceService.solve(problem);
    }
}
