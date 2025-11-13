package com.fesup.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteDTO {
    
    private Long id;
    private String titre;
    private String description;
    private String type;
    private String demiJournee;
    private Integer capaciteMax;
}
