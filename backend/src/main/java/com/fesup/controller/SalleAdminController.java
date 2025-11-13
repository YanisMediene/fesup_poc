package com.fesup.controller;

import com.fesup.dto.SalleDTO;
import com.fesup.entity.Salle;
import com.fesup.repository.SalleRepository;
import com.fesup.repository.SessionRepository;
import com.fesup.repository.AffectationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/salles")
@PreAuthorize("hasRole('ADMIN')")
public class SalleAdminController {
    
    @Autowired
    private SalleRepository salleRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private AffectationRepository affectationRepository;
    
    @GetMapping
    public ResponseEntity<List<SalleDTO>> getAllSalles() {
        List<SalleDTO> salles = salleRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(salles);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SalleDTO> getSalleById(@PathVariable Long id) {
        return salleRepository.findById(id)
            .map(salle -> ResponseEntity.ok(convertToDTO(salle)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<SalleDTO> createSalle(@RequestBody SalleDTO dto) {
        Salle salle = new Salle();
        salle.setNom(dto.getNom());
        salle.setCapacite(dto.getCapacite());
        salle.setBatiment(dto.getBatiment());
        salle.setEquipements(dto.getEquipements());
        
        Salle saved = salleRepository.save(salle);
        return ResponseEntity.ok(convertToDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SalleDTO> updateSalle(@PathVariable Long id, @RequestBody SalleDTO dto) {
        return salleRepository.findById(id)
            .map(salle -> {
                salle.setNom(dto.getNom());
                salle.setCapacite(dto.getCapacite());
                salle.setBatiment(dto.getBatiment());
                salle.setEquipements(dto.getEquipements());
                Salle updated = salleRepository.save(salle);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalle(@PathVariable Long id) {
        if (salleRepository.existsById(id)) {
            salleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Supprime toutes les salles (et en cascade les sessions et affectations)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllSalles() {
        long salleCount = salleRepository.count();
        long sessionCount = sessionRepository.count();
        long affectationCount = affectationRepository.count();
        
        // Suppression en cascade : affectations -> sessions -> salles
        if (affectationCount > 0) {
            affectationRepository.deleteAll();
        }
        if (sessionCount > 0) {
            sessionRepository.deleteAll();
        }
        salleRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Toutes les salles ont été supprimées avec succès");
        response.put("sallesSupprimes", salleCount);
        response.put("sessionsSupprimes", sessionCount);
        response.put("affectationsSupprimes", affectationCount);
        
        return ResponseEntity.ok(response);
    }
    
    private SalleDTO convertToDTO(Salle salle) {
        SalleDTO dto = new SalleDTO();
        dto.setId(salle.getId());
        dto.setNom(salle.getNom());
        dto.setCapacite(salle.getCapacite());
        dto.setBatiment(salle.getBatiment());
        dto.setEquipements(salle.getEquipements());
        return dto;
    }
}
