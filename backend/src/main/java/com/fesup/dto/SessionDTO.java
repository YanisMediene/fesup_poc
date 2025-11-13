package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;
    private Long activiteId;
    private String activiteTitre;
    private String activiteType;
    private Long salleId;
    private String salleNom;
    private Integer salleCapacite;
    private Long creneauId;
    private String creneauLibelle;
    private String creneauDemiJournee;
    private Integer capaciteDisponible;
}
