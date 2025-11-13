package com.fesup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportReportDTO {
    private int totalLines;
    private int successCount;
    private int errorCount;
    private List<ImportErrorDTO> errors = new ArrayList<>();
}
