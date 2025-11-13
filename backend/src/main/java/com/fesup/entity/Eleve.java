package com.fesup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fesup.enums.DemiJournee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "eleves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Eleve {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String idNational;  // Ex: "120890177FA"
    
    @Column(nullable = false)
    private String nom;
    
    @Column(nullable = false)
    private String prenom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lycee_id", nullable = false)
    private Lycee lycee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemiJournee demiJournee;
    
    @OneToMany(mappedBy = "eleve", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Éviter la sérialisation circulaire
    private List<Voeu> voeux = new ArrayList<>();
    
    @OneToOne(mappedBy = "eleve", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Ticket ticket;
    
    @Column(nullable = false)
    private boolean voeuxSoumis = false;
    
    private LocalDateTime dateSoumission;
    
    // Override equals/hashCode pour éviter LazyInitializationException avec Timefold
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Eleve)) return false;
        Eleve eleve = (Eleve) o;
        return Objects.equals(id, eleve.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
