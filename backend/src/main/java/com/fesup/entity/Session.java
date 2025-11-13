package com.fesup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;
    
    @Column(nullable = false)
    private Integer capaciteDisponible;
    
    /**
     * Initialise la capacité disponible avec le minimum entre
     * la capacité de la salle et la capacité max de l'activité
     */
    @PrePersist
    @PreUpdate
    public void calculerCapacite() {
        if (salle != null && activite != null) {
            this.capaciteDisponible = Math.min(salle.getCapacite(), activite.getCapaciteMax());
        }
    }
}
