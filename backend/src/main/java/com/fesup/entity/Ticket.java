package com.fesup.entity;

import com.fesup.enums.StatutTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "eleve_id", unique = true, nullable = false)
    private Eleve eleve;
    
    @Column(nullable = false)
    private String cheminFichier; // ex: "2025/eleve_123.pdf"
    
    @Column(nullable = false)
    private LocalDateTime dateGeneration;
    
    @Column(nullable = false)
    private Long tailleFichier; // en bytes
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTicket statut;
}
