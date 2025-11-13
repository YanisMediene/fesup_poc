package com.fesup.entity;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "affectations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PlanningEntity
public class Affectation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id")
    @PlanningVariable
    private Session assignedSession;
    
    // Constructeur pour Timefold (sans l'ID)
    public Affectation(Eleve eleve, Session assignedSession) {
        this.eleve = eleve;
        this.assignedSession = assignedSession;
    }
    
    // Override equals/hashCode pour Timefold (utiliser l'élève comme identifiant unique)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Affectation)) return false;
        Affectation that = (Affectation) o;
        return Objects.equals(eleve, that.eleve);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eleve);
    }
}
