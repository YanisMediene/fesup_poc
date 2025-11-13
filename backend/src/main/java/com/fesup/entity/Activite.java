package com.fesup.entity;

import com.fesup.enums.DemiJournee;
import com.fesup.enums.TypeActivite;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "activites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeActivite type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemiJournee demiJournee;
    
    private Integer capaciteMax;
}
