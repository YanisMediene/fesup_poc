package com.fesup.controller;

import com.fesup.dto.ActiviteDTO;
import com.fesup.entity.Activite;
import com.fesup.enums.DemiJournee;
import com.fesup.enums.TypeActivite;
import com.fesup.repository.ActiviteRepository;
import com.fesup.repository.SessionRepository;
import com.fesup.repository.AffectationRepository;
import com.fesup.repository.VoeuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/activites")
@PreAuthorize("hasRole('ADMIN')")
public class ActiviteAdminController {
    
    @Autowired
    private ActiviteRepository activiteRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private AffectationRepository affectationRepository;
    
    @Autowired
    private VoeuRepository voeuRepository;
    
    @GetMapping
    public ResponseEntity<List<ActiviteDTO>> getAllActivites() {
        List<ActiviteDTO> activites = activiteRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(activites);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ActiviteDTO> getActiviteById(@PathVariable Long id) {
        return activiteRepository.findById(id)
            .map(activite -> ResponseEntity.ok(convertToDTO(activite)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ActiviteDTO> createActivite(@RequestBody ActiviteDTO dto) {
        Activite activite = new Activite();
        activite.setTitre(dto.getTitre());
        activite.setDescription(dto.getDescription());
        activite.setType(TypeActivite.valueOf(dto.getType()));
        activite.setDemiJournee(DemiJournee.valueOf(dto.getDemiJournee()));
        activite.setCapaciteMax(dto.getCapaciteMax());
        
        Activite saved = activiteRepository.save(activite);
        return ResponseEntity.ok(convertToDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ActiviteDTO> updateActivite(@PathVariable Long id, @RequestBody ActiviteDTO dto) {
        return activiteRepository.findById(id)
            .map(activite -> {
                activite.setTitre(dto.getTitre());
                activite.setDescription(dto.getDescription());
                activite.setType(TypeActivite.valueOf(dto.getType()));
                activite.setDemiJournee(DemiJournee.valueOf(dto.getDemiJournee()));
                activite.setCapaciteMax(dto.getCapaciteMax());
                Activite updated = activiteRepository.save(activite);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivite(@PathVariable Long id) {
        if (activiteRepository.existsById(id)) {
            activiteRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Supprime toutes les activités (et en cascade les voeux, sessions et affectations)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllActivites() {
        long activiteCount = activiteRepository.count();
        long voeuCount = voeuRepository.count();
        long sessionCount = sessionRepository.count();
        long affectationCount = affectationRepository.count();
        
        // Suppression en cascade : voeux -> affectations -> sessions -> activités
        if (voeuCount > 0) {
            voeuRepository.deleteAll();
        }
        if (affectationCount > 0) {
            affectationRepository.deleteAll();
        }
        if (sessionCount > 0) {
            sessionRepository.deleteAll();
        }
        activiteRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Toutes les activités ont été supprimées avec succès");
        response.put("activitesSupprimes", activiteCount);
        response.put("voeuxSupprimes", voeuCount);
        response.put("sessionsSupprimes", sessionCount);
        response.put("affectationsSupprimes", affectationCount);
        
        return ResponseEntity.ok(response);
    }
    
    private ActiviteDTO convertToDTO(Activite activite) {
        ActiviteDTO dto = new ActiviteDTO();
        dto.setId(activite.getId());
        dto.setTitre(activite.getTitre());
        dto.setDescription(activite.getDescription());
        dto.setType(activite.getType().name());
        dto.setDemiJournee(activite.getDemiJournee().name());
        dto.setCapaciteMax(activite.getCapaciteMax());
        return dto;
    }
}
