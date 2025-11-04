package com.szakdolgozat.scheduler.cloudbalancing.service;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.domain.Computer;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CloudBalanceService {

    private final SolverFactory<CloudBalance> solverFactory;

    public CloudBalanceService(@Qualifier("cloudBalanceSolverFactory") SolverFactory<CloudBalance> solverFactory){
        this.solverFactory = solverFactory;
    }

    public CloudBalance solve(CloudBalance problem) {
        Solver<CloudBalance> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }

    public CloudBalance generateDemoData() {
        List<Computer> computerList = new ArrayList<>();
        int idc = 0;
        computerList.add(new Computer(idc++,4, 8, 10, 100));
        computerList.add(new Computer(idc++,8, 16, 20, 200));
        computerList.add(new Computer(idc++,12, 24, 30, 300));
        computerList.add(new Computer(idc++,16, 32, 40, 500));

        List<CloudProcess> processList = new ArrayList<>();
        long id = 0;
        processList.add(new CloudProcess(id++,1, 2, 2, null));
        processList.add(new CloudProcess(id++,2, 4, 3, null));
        processList.add(new CloudProcess(id++,1, 3, 2, null));
        processList.add(new CloudProcess(id++,3, 5, 4, null));
        processList.add(new CloudProcess(id++,2, 3, 3, null));
        processList.add(new CloudProcess(id++,4, 6, 5, null));

        return new CloudBalance(processList, computerList);
    }
}
