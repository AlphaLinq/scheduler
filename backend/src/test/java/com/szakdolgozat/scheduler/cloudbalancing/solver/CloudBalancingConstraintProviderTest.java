package com.szakdolgozat.scheduler.cloudbalancing.solver;

import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.domain.Computer;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

class CloudBalancingConstraintProviderTest {

    private final ConstraintVerifier<CloudBalancingConstraintProvider, CloudBalance> constraintVerifier =
            ConstraintVerifier.build(new CloudBalancingConstraintProvider(), CloudBalance.class, CloudProcess.class);

    @Test
    void cpuExceeded_penalized() {
        Computer computer = new Computer(0, 4, 16, 20, 100);
        CloudProcess p1 = new CloudProcess(0L, 3, 2, 2, computer);
        CloudProcess p2 = new CloudProcess(1L, 3, 2, 2, computer);
        // CPU=6 > 4 => hard -1, both assigned to same computer => soft -100

        constraintVerifier.verifyThat()
                .given(computer, p1, p2)
                .scores(HardSoftScore.of(-1, -100));
    }

    @Test
    void allWithinCapacity_onlySoftCost() {
        Computer computer = new Computer(0, 8, 16, 20, 200);
        CloudProcess p1 = new CloudProcess(0L, 3, 2, 2, computer);
        CloudProcess p2 = new CloudProcess(1L, 3, 2, 2, computer);
        // CPU=6 <= 8, Mem=4 <= 16, BW=4 <= 20 => no hard penalty, cost=200 soft penalty

        constraintVerifier.verifyThat()
                .given(computer, p1, p2)
                .scores(HardSoftScore.of(0, -200));
    }

    @Test
    void unusedComputer_noCost() {
        Computer computer = new Computer(0, 8, 16, 20, 200);

        constraintVerifier.verifyThat()
                .given(computer)
                .scores(HardSoftScore.of(0, 0));
    }

    @Test
    void memoryExceeded_penalized() {
        Computer computer = new Computer(0, 8, 4, 20, 100);
        CloudProcess p1 = new CloudProcess(0L, 1, 3, 2, computer);
        CloudProcess p2 = new CloudProcess(1L, 1, 3, 2, computer);
        // Memory=6 > 4 => hard -1

        constraintVerifier.verifyThat()
                .given(computer, p1, p2)
                .scores(HardSoftScore.of(-1, -100));
    }

    @Test
    void bandwidthExceeded_penalized() {
        Computer computer = new Computer(0, 8, 16, 5, 100);
        CloudProcess p1 = new CloudProcess(0L, 1, 2, 4, computer);
        CloudProcess p2 = new CloudProcess(1L, 1, 2, 4, computer);
        // BW=8 > 5 => hard -1

        constraintVerifier.verifyThat()
                .given(computer, p1, p2)
                .scores(HardSoftScore.of(-1, -100));
    }
}
