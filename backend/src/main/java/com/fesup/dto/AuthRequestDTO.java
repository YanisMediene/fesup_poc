package com.fesup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {
    
    @NotBlank(message = "L'ID national est obligatoire")
    private String idNational;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
}
