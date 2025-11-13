package com.fesup.config;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.SolverConfig;
import com.fesup.solver.AffectationConstraintProvider;
import com.fesup.solver.AffectationSolution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class TimefoldConfig {
    
    @Bean
    public SolverConfig solverConfig() {
        return new SolverConfig()
            .withSolutionClass(AffectationSolution.class)
            .withEntityClasses(com.fesup.entity.Affectation.class)
            .withConstraintProviderClass(AffectationConstraintProvider.class)
            .withTerminationSpentLimit(Duration.ofMinutes(5)); // 5 minutes max
    }
    
    @Bean
    public SolverManager<AffectationSolution, UUID> solverManager(SolverConfig solverConfig) {
        return SolverManager.create(solverConfig);
    }
}
