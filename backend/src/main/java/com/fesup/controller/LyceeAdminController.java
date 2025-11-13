package com.fesup.controller;

import com.fesup.dto.LyceeDTO;
import com.fesup.entity.Lycee;
import com.fesup.repository.LyceeRepository;
import com.fesup.repository.EleveRepository;
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
@RequestMapping("/api/admin/lycees")
@PreAuthorize("hasRole('ADMIN')")
public class LyceeAdminController {
    
    @Autowired
    private LyceeRepository lyceeRepository;
    
    @Autowired
    private EleveRepository eleveRepository;
    
    @Autowired
    private VoeuRepository voeuRepository;
    
    @GetMapping
    public ResponseEntity<List<LyceeDTO>> getAllLycees() {
        List<LyceeDTO> lycees = lyceeRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(lycees);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LyceeDTO> getLyceeById(@PathVariable Long id) {
        return lyceeRepository.findById(id)
            .map(lycee -> ResponseEntity.ok(convertToDTO(lycee)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<LyceeDTO> createLycee(@RequestBody LyceeDTO dto) {
        Lycee lycee = new Lycee();
        lycee.setNom(dto.getNom());
        lycee.setVille(dto.getVille());
        lycee.setCodePostal(dto.getCodePostal());
        
        Lycee saved = lyceeRepository.save(lycee);
        return ResponseEntity.ok(convertToDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LyceeDTO> updateLycee(@PathVariable Long id, @RequestBody LyceeDTO dto) {
        return lyceeRepository.findById(id)
            .map(lycee -> {
                lycee.setNom(dto.getNom());
                lycee.setVille(dto.getVille());
                lycee.setCodePostal(dto.getCodePostal());
                Lycee updated = lyceeRepository.save(lycee);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLycee(@PathVariable Long id) {
        if (lyceeRepository.existsById(id)) {
            lyceeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Supprime tous les lycées (et en cascade les élèves et leurs vœux)
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllLycees() {
        long lyceeCount = lyceeRepository.count();
        long eleveCount = eleveRepository.count();
        long voeuCount = voeuRepository.count();
        
        // Suppression en cascade : voeux -> eleves -> lycées
        if (voeuCount > 0) {
            voeuRepository.deleteAll();
        }
        if (eleveCount > 0) {
            eleveRepository.deleteAll();
        }
        lyceeRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tous les lycées ont été supprimés avec succès");
        response.put("lyceesSupprimes", lyceeCount);
        response.put("elevesSupprimes", eleveCount);
        response.put("voeuxSupprimes", voeuCount);
        
        return ResponseEntity.ok(response);
    }
    
    private LyceeDTO convertToDTO(Lycee lycee) {
        LyceeDTO dto = new LyceeDTO();
        dto.setId(lycee.getId());
        dto.setNom(lycee.getNom());
        dto.setVille(lycee.getVille());
        dto.setCodePostal(lycee.getCodePostal());
        return dto;
    }
}
