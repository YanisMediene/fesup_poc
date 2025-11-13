package com.fesup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lycees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lycee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nom;
    
    private String ville;
    
    private String codePostal;
    
    @OneToMany(mappedBy = "lycee")
    @JsonIgnore  // Éviter la sérialisation circulaire et LazyInitializationException
    private List<Eleve> eleves = new ArrayList<>();
}
