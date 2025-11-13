package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportErrorDTO {
    private int lineNumber;
    private String reason;
    private String lineContent;
}
