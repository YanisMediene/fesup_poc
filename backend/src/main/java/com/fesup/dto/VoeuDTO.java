package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoeuDTO {
    private Long id;
    private Long eleveId;
    private Long activiteId;
    private Integer ordre;
}
