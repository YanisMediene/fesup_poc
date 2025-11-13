package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreneauDTO {
    private Long id;
    private String libelle;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String demiJournee;
}
