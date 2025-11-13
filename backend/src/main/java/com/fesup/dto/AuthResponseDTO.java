package com.fesup.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private Long id;
    private String idNational;
    private String nom;
    private String prenom;
    private String lycee;
    private String demiJournee;
    private boolean voeuxDejasoumis;
}
