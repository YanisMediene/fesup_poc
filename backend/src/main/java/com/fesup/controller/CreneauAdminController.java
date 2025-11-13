package com.fesup.controller;

import com.fesup.dto.CreneauDTO;
import com.fesup.entity.Creneau;
import com.fesup.entity.Session;
import com.fesup.enums.DemiJournee;
import com.fesup.repository.CreneauRepository;
import com.fesup.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/creneaux")
@PreAuthorize("hasRole('ADMIN')")
public class CreneauAdminController {
    
    @Autowired
    private CreneauRepository creneauRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @GetMapping
    public ResponseEntity<List<CreneauDTO>> getAllCreneaux() {
        List<CreneauDTO> creneaux = creneauRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(creneaux);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CreneauDTO> getCreneauById(@PathVariable Long id) {
        return creneauRepository.findById(id)
            .map(creneau -> ResponseEntity.ok(convertToDTO(creneau)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<CreneauDTO> createCreneau(@RequestBody CreneauDTO dto) {
        Creneau creneau = new Creneau();
        creneau.setLibelle(dto.getLibelle());
        creneau.setHeureDebut(dto.getHeureDebut());
        creneau.setHeureFin(dto.getHeureFin());
        creneau.setDemiJournee(DemiJournee.valueOf(dto.getDemiJournee()));
        
        Creneau saved = creneauRepository.save(creneau);
        return ResponseEntity.ok(convertToDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CreneauDTO> updateCreneau(@PathVariable Long id, @RequestBody CreneauDTO dto) {
        return creneauRepository.findById(id)
            .map(creneau -> {
                creneau.setLibelle(dto.getLibelle());
                creneau.setHeureDebut(dto.getHeureDebut());
                creneau.setHeureFin(dto.getHeureFin());
                creneau.setDemiJournee(DemiJournee.valueOf(dto.getDemiJournee()));
                Creneau updated = creneauRepository.save(creneau);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCreneau(@PathVariable Long id) {
        if (!creneauRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Vérifier si le créneau est utilisé dans des sessions
        List<Session> sessions = sessionRepository.findByCreneauId(id);
        if (!sessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "error", "Ce créneau est utilisé par " + sessions.size() + " session(s)",
                    "nbSessions", sessions.size()
                ));
        }
        
        creneauRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/all")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteAllCreneaux() {
        // Supprimer toutes les sessions d'abord (cascade)
        long nbSessions = sessionRepository.count();
        sessionRepository.deleteAll();
        
        // Supprimer tous les créneaux
        long nbCreneaux = creneauRepository.count();
        creneauRepository.deleteAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Suppression réussie");
        result.put("creneauxSupprimes", nbCreneaux);
        result.put("sessionsSupprimes", nbSessions);
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/import-csv")
    public ResponseEntity<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                total++;
                String[] values = line.split(",");
                
                if (values.length < 4) {
                    errors.add("Ligne " + total + " : Format invalide (attendu: libelle,heureDebut,heureFin,demiJournee)");
                    continue;
                }
                
                try {
                    String libelle = values[0].trim();
                    LocalTime heureDebut = LocalTime.parse(values[1].trim());
                    LocalTime heureFin = LocalTime.parse(values[2].trim());
                    DemiJournee demiJournee = DemiJournee.valueOf(values[3].trim());
                    
                    Creneau creneau = new Creneau();
                    creneau.setLibelle(libelle);
                    creneau.setHeureDebut(heureDebut);
                    creneau.setHeureFin(heureFin);
                    creneau.setDemiJournee(demiJournee);
                    
                    creneauRepository.save(creneau);
                    success++;
                    
                } catch (Exception e) {
                    errors.add("Ligne " + total + " : " + e.getMessage());
                }
            }
            
            result.put("success", success);
            result.put("total", total);
            result.put("errors", errors);
            result.put("message", success + "/" + total + " créneaux importés avec succès");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", "Erreur lors de la lecture du fichier : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    private CreneauDTO convertToDTO(Creneau creneau) {
        CreneauDTO dto = new CreneauDTO();
        dto.setId(creneau.getId());
        dto.setLibelle(creneau.getLibelle());
        dto.setHeureDebut(creneau.getHeureDebut());
        dto.setHeureFin(creneau.getHeureFin());
        dto.setDemiJournee(creneau.getDemiJournee().name());
        return dto;
    }
}
