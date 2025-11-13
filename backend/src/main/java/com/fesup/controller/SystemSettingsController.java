package com.fesup.controller;

import com.fesup.repository.*;
import com.fesup.service.CsvExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin/system")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SystemSettingsController {
    
    @Autowired
    private VoeuRepository voeuRepository;
    
    @Autowired
    private AffectationRepository affectationRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private EleveRepository eleveRepository;
    
    @Autowired
    private ActiviteRepository activiteRepository;
    
    @Autowired
    private SalleRepository salleRepository;
    
    @Autowired
    private CreneauRepository creneauRepository;
    
    @Autowired
    private LyceeRepository lyceeRepository;
    
    @Autowired
    private CsvExportService csvExportService;
    
    /**
     * Supprime TOUTES les données (sauf les admins)
     * Ordre strict pour respecter les contraintes FK
     */
    @DeleteMapping("/purge-all")
    public ResponseEntity<Map<String, Object>> purgeAllData() {
        Map<String, Long> counts = new HashMap<>();
        
        // Compter avant suppression
        counts.put("voeux", voeuRepository.count());
        counts.put("affectations", affectationRepository.count());
        counts.put("sessions", sessionRepository.count());
        counts.put("eleves", eleveRepository.count());
        counts.put("activites", activiteRepository.count());
        counts.put("salles", salleRepository.count());
        counts.put("creneaux", creneauRepository.count());
        counts.put("lycees", lyceeRepository.count());
        
        // Suppression en cascade (ordre strict pour FK)
        voeuRepository.deleteAll();
        affectationRepository.deleteAll();
        sessionRepository.deleteAll();
        eleveRepository.deleteAll();
        activiteRepository.deleteAll();
        salleRepository.deleteAll();
        creneauRepository.deleteAll();
        lyceeRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Toutes les données ont été supprimées avec succès");
        response.put("counts", counts);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Récupère les statistiques système
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getSystemStats() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("voeux", voeuRepository.count());
        stats.put("affectations", affectationRepository.count());
        stats.put("sessions", sessionRepository.count());
        stats.put("eleves", eleveRepository.count());
        stats.put("activites", activiteRepository.count());
        stats.put("salles", salleRepository.count());
        stats.put("creneaux", creneauRepository.count());
        stats.put("lycees", lyceeRepository.count());
        
        return ResponseEntity.ok(stats);
    }
    
    // ============================================
    // EXPORTS CSV
    // ============================================
    
    /**
     * Exporte tous les élèves en CSV
     */
    @GetMapping("/export/eleves")
    public ResponseEntity<String> exporterEleves() {
        String csv = csvExportService.exporterEleves();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "eleves.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte toutes les activités en CSV
     */
    @GetMapping("/export/activites")
    public ResponseEntity<String> exporterActivites() {
        String csv = csvExportService.exporterActivites();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "activites.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte toutes les salles en CSV
     */
    @GetMapping("/export/salles")
    public ResponseEntity<String> exporterSalles() {
        String csv = csvExportService.exporterSalles();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "salles.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte tous les créneaux en CSV
     */
    @GetMapping("/export/creneaux")
    public ResponseEntity<String> exporterCreneaux() {
        String csv = csvExportService.exporterCreneaux();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "creneaux.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte tous les lycées en CSV
     */
    @GetMapping("/export/lycees")
    public ResponseEntity<String> exporterLycees() {
        String csv = csvExportService.exporterLycees();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "lycees.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte tous les vœux en CSV
     */
    @GetMapping("/export/voeux")
    public ResponseEntity<String> exporterVoeux() {
        String csv = csvExportService.exporterVoeux();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "voeux.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte toutes les sessions en CSV
     */
    @GetMapping("/export/sessions")
    public ResponseEntity<String> exporterSessions() {
        String csv = csvExportService.exporterSessions();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "sessions.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte toutes les affectations en CSV
     */
    @GetMapping("/export/affectations")
    public ResponseEntity<String> exporterAffectations() {
        String csv = csvExportService.exporterAffectations();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "affectations.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
    
    /**
     * Exporte toutes les données dans un fichier ZIP
     */
    @GetMapping("/export/all")
    public ResponseEntity<byte[]> exporterToutesLesDonnees() {
        try {
            byte[] zipData = csvExportService.exporterToutesLesDonnees();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "export_fesup_" + timestamp + ".zip";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
