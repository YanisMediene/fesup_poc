package com.fesup.controller;

import com.fesup.dto.ImportReportDTO;
import com.fesup.service.AdminImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImportController {
    
    @Autowired
    private AdminImportService adminImportService;
    
    @PostMapping("/eleves")
    public ResponseEntity<ImportReportDTO> importEleves(@RequestParam("file") MultipartFile file) {
        ImportReportDTO report = adminImportService.importEleves(file);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/activites")
    public ResponseEntity<ImportReportDTO> importActivites(@RequestParam("file") MultipartFile file) {
        ImportReportDTO report = adminImportService.importActivites(file);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/salles")
    public ResponseEntity<ImportReportDTO> importSalles(@RequestParam("file") MultipartFile file) {
        ImportReportDTO report = adminImportService.importSalles(file);
        return ResponseEntity.ok(report);
    }
}
