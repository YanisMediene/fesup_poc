package com.fesup.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoeuxSubmissionResponseDTO {
    
    private boolean success;
    private String message;
    private LocalDateTime dateSoumission;
}
