package com.szakdolgozat.scheduler.config;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.solver.CloudBalancingConstraintProvider;
import com.szakdolgozat.scheduler.timetable.domain.Lesson;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import com.szakdolgozat.scheduler.timetable.solver.TimeTableConstraintProvider;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OptaPlannerConfig {

    @Bean(name = "timeTableSolver")
    public Solver<TimeTable> timeTableSolver() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(TimeTable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimeTableConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(30));

        return SolverFactory.<TimeTable>create(config).buildSolver();
    }

    @Bean(name = "cloudBalanceSolver")
    public Solver<CloudBalance> cloudBalanceSolver() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(CloudBalance.class)
                .withEntityClasses(CloudProcess.class)
                .withConstraintProviderClass(CloudBalancingConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(5));

        return SolverFactory.<CloudBalance>create(config).buildSolver();
    }
}
