package com.szakdolgozat.scheduler.cloudbalancing.solver;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.domain.Computer;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.util.function.Function;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;
import static org.optaplanner.core.api.score.stream.Joiners.equal;


public class CloudBalancingConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredCpuPowerTotal(constraintFactory),
                requiredMemoryTotal(constraintFactory),
                requiredBandwidthTotal(constraintFactory),
                computerCost(constraintFactory)
        };
    }

    private Constraint requiredCpuPowerTotal(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredCpuPower))
                .filter((computer, requiredCpuPower) -> requiredCpuPower > computer.getCpuPower())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("requiredCpuPowerTotal constraint");
    }

    private Constraint requiredMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredMemory))
                .filter((computer, requiredMemory) -> requiredMemory > computer.getMemory())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("requiredMemoryTotal constraint");
    }

    private Constraint requiredBandwidthTotal(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredBandwidth))
                .filter((computer, requiredBandwidth) -> requiredBandwidth > computer.getNetworkBandwidth())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("requiredBandwidth constraint");
    }

    private Constraint computerCost(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Computer.class)
                .ifExists(CloudProcess.class, equal(Function.identity(), CloudProcess::getComputer))
                .penalize(HardSoftScore.ONE_SOFT, Computer::getCost)
                .asConstraint("computerCost constraint");
    }
}
