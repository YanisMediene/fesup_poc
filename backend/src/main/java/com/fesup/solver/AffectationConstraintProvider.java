package com.fesup.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import com.fesup.entity.Affectation;
import com.fesup.entity.Voeu;

import static ai.timefold.solver.core.api.score.stream.Joiners.*;

public class AffectationConstraintProvider implements ConstraintProvider {
    
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            // Contraintes dures
            capaciteSalle(constraintFactory),
            capaciteActivite(constraintFactory),
            eleveUnique(constraintFactory),
            maxRepetitionActivite(constraintFactory),
            
            // Contraintes douces
            voeux1_2Obligatoire(constraintFactory),
            voeux3_4_5Bonus(constraintFactory)
        };
    }
    
    // === CONTRAINTES DURES ===
    
    /**
     * Une salle ne peut pas accueillir plus d'élèves que sa capacité
     */
    private Constraint capaciteSalle(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Affectation.class)
            .filter(affectation -> affectation.getAssignedSession() != null)
            .groupBy(
                Affectation::getAssignedSession,
                ConstraintCollectors.count()
            )
            .filter((session, count) -> count > session.getSalle().getCapacite())
            .penalize(HardSoftScore.ONE_HARD,
                (session, count) -> count - session.getSalle().getCapacite())
            .asConstraint("Capacité salle dépassée");
    }
    
    /**
     * Une activité ne peut pas avoir plus de participants que sa capacité max
     */
    private Constraint capaciteActivite(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Affectation.class)
            .filter(affectation -> affectation.getAssignedSession() != null)
            .groupBy(
                affectation -> affectation.getAssignedSession().getActivite(),
                ConstraintCollectors.count()
            )
            .filter((activite, count) -> count > activite.getCapaciteMax())
            .penalize(HardSoftScore.ONE_HARD,
                (activite, count) -> count - activite.getCapaciteMax())
            .asConstraint("Capacité activité dépassée");
    }
    
    /**
     * Un élève ne peut être affecté qu'à une seule session par créneau horaire
     * (Il peut avoir 4 sessions dans sa demi-journée, une par créneau)
     */
    private Constraint eleveUnique(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Affectation.class)
            .filter(affectation -> affectation.getAssignedSession() != null)
            .join(Affectation.class,
                equal(Affectation::getEleve),
                equal(affectation -> affectation.getAssignedSession().getCreneau().getHeureDebut()),
                equal(affectation -> affectation.getAssignedSession().getCreneau().getHeureFin()),
                filtering((aff1, aff2) -> aff2.getAssignedSession() != null 
                    && aff1 != aff2)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Élève affecté à 2 sessions au même créneau");
    }
    
    /**
     * Une même activité ne peut pas être répétée plus de 5 fois par demi-journée
     */
    private Constraint maxRepetitionActivite(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Affectation.class)
            .filter(affectation -> affectation.getAssignedSession() != null)
            .groupBy(
                affectation -> affectation.getAssignedSession().getActivite(),
                affectation -> affectation.getAssignedSession().getCreneau().getDemiJournee(),
                ConstraintCollectors.countDistinct(affectation -> affectation.getAssignedSession().getId())
            )
            .filter((activite, demiJournee, nbSessions) -> nbSessions > 5)
            .penalize(HardSoftScore.ONE_HARD,
                (activite, demiJournee, nbSessions) -> nbSessions - 5)
            .asConstraint("Activité répétée plus de 5 fois");
    }
    
    // === CONTRAINTES DOUCES ===
    
    /**
     * Les vœux de priorité 1 et 2 sont OBLIGATOIRES (forte pénalité si non satisfaits)
     */
    private Constraint voeux1_2Obligatoire(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Voeu.class)
            .filter(voeu -> voeu.getPriorite() <= 2)
            .ifNotExists(Affectation.class,
                equal(Voeu::getEleve, Affectation::getEleve),
                equal(Voeu::getActivite, 
                    affectation -> affectation.getAssignedSession() != null 
                        ? affectation.getAssignedSession().getActivite() 
                        : null)
            )
            .penalize(HardSoftScore.ONE_SOFT, voeu -> 1000 * (3 - voeu.getPriorite()))
            .asConstraint("Vœux prioritaires 1-2 non satisfaits");
    }
    
    /**
     * Les vœux de priorité 3, 4 et 5 donnent des BONUS (récompense si satisfaits)
     */
    private Constraint voeux3_4_5Bonus(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Voeu.class)
            .filter(voeu -> voeu.getPriorite() >= 3 && voeu.getPriorite() <= 5)
            .ifExists(Affectation.class,
                equal(Voeu::getEleve, Affectation::getEleve),
                equal(Voeu::getActivite,
                    affectation -> affectation.getAssignedSession() != null
                        ? affectation.getAssignedSession().getActivite()
                        : null)
            )
            .reward(HardSoftScore.ONE_SOFT, voeu -> 100 * (6 - voeu.getPriorite()))
            .asConstraint("Bonus vœux 3-4-5 satisfaits");
    }
}
