package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LyceeDTO {
    private Long id;
    private String nom;
    private String ville;
    private String codePostal;
}
