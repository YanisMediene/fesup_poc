package com.fesup.solver;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.fesup.entity.Affectation;
import com.fesup.entity.Session;
import com.fesup.entity.Voeu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@PlanningSolution
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffectationSolution {
    
    @ProblemFactCollectionProperty
    private List<Voeu> voeux;
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Session> sessions;
    
    @PlanningEntityCollectionProperty
    private List<Affectation> affectations;
    
    @PlanningScore
    private HardSoftScore score;
    
    // Constructeur sans le score (calculé par Timefold)
    public AffectationSolution(List<Voeu> voeux, List<Session> sessions, List<Affectation> affectations) {
        this.voeux = voeux;
        this.sessions = sessions;
        this.affectations = affectations;
    }
}
