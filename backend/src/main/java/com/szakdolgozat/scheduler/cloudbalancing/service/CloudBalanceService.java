package com.szakdolgozat.scheduler.cloudbalancing.service;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.domain.Computer;
import com.szakdolgozat.scheduler.cloudbalancing.solver.CloudBalancingConstraintProvider;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class CloudBalanceService {

    private final Solver<CloudBalance> solver;

    public CloudBalanceService(@Qualifier("cloudBalanceSolver") Solver<CloudBalance> solver){
        this.solver = solver;
    }

    public CloudBalance solve(CloudBalance problem) {
        return solver.solve(problem);
    }

    public CloudBalance generateDemoData() {
        List<Computer> computerList = new ArrayList<>();
        computerList.add(new Computer(1, 2, 4, 100));
        computerList.add(new Computer(2, 2, 6, 120));
        computerList.add(new Computer(3, 4, 8, 180));
        computerList.add(new Computer(4, 4, 12, 200));

        List<CloudProcess> processList = new ArrayList<>();
        long id = 0;
        processList.add(new CloudProcess(id++,1, 1, 1, null));
        processList.add(new CloudProcess(id++,2, 2, 2, null));
        processList.add(new CloudProcess(id++,3, 1, 3, null));
        processList.add(new CloudProcess(id++,4, 2, 1, null));
        processList.add(new CloudProcess(id++,5, 1, 2, null));
        processList.add(new CloudProcess(id++,6, 3, 3, null));

        return new CloudBalance(processList, computerList);
    }
}
