package com.szakdolgozat.scheduler.config;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudBalance;
import com.szakdolgozat.scheduler.cloudbalancing.domain.CloudProcess;
import com.szakdolgozat.scheduler.cloudbalancing.solver.CloudBalancingConstraintProvider;
import com.szakdolgozat.scheduler.timetable.domain.Lesson;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import com.szakdolgozat.scheduler.timetable.solver.TimeTableConstraintProvider;
import com.szakdolgozat.scheduler.vehiclerouting.domain.Customer;
import com.szakdolgozat.scheduler.vehiclerouting.domain.VehicleRoutingSolution;
import com.szakdolgozat.scheduler.vehiclerouting.solver.VehicleRoutingConstraintProvider;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OptaPlannerConfig {

    @Bean(name = "timeTableSolverFactory")
    public SolverFactory<TimeTable> timeTableSolverFactory() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(TimeTable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimeTableConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(30));

        return SolverFactory.<TimeTable>create(config);
    }

    @Bean(name = "cloudBalanceSolverFactory")
    public SolverFactory<CloudBalance> cloudBalanceSolverFactory() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(CloudBalance.class)
                .withEntityClasses(CloudProcess.class)
                .withConstraintProviderClass(CloudBalancingConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(20));

        return SolverFactory.<CloudBalance>create(config);
    }

    @Bean(name = "vehicleRoutingSolverFactory")
    public SolverFactory<VehicleRoutingSolution> vehicleRoutingSolverFactory() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(VehicleRoutingSolution.class)
                .withEntityClasses(Customer.class)
                .withConstraintProviderClass(VehicleRoutingConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(30));

        return SolverFactory.<VehicleRoutingSolution>create(config);
    }
}
