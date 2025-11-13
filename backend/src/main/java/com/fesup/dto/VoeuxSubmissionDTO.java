package com.fesup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoeuxSubmissionDTO {
    
    @NotNull(message = "L'ID de l'élève est obligatoire")
    private Long eleveId;
    
    @NotNull(message = "Le vœu 1 est obligatoire")
    private Long conferenceVoeu1;
    
    @NotNull(message = "Le vœu 2 est obligatoire")
    private Long conferenceVoeu2;
    
    @NotNull(message = "Le vœu 3 est obligatoire")
    private Long activiteVoeu3;
    
    @NotNull(message = "Le vœu 4 est obligatoire")
    private Long activiteVoeu4;
    
    @NotNull(message = "Le vœu 5 est obligatoire")
    private Long activiteVoeu5;
}
