package com.fesup.entity;

import com.fesup.enums.TypeVoeu;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "voeux")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voeu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eleve_id", nullable = false)
    private Eleve eleve;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;
    
    @Column(nullable = false)
    private Integer priorite;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeVoeu typeVoeu;
}
