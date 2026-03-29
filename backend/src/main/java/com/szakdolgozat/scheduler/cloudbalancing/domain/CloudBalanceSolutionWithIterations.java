package com.szakdolgozat.scheduler.cloudbalancing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloudBalanceSolutionWithIterations {
    private CloudBalance finalSolution;
    private List<CloudBalanceIteration> iterations;
}
