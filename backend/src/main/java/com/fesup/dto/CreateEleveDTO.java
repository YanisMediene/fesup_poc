package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEleveDTO {
    private String nom;
    private String prenom;
    private Long lyceeId;
    private String demiJournee; // "MATIN" ou "APRES_MIDI"
}
