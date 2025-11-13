package com.fesup.controller;

import com.fesup.dto.ActiviteDTO;
import com.fesup.entity.Activite;
import com.fesup.repository.ActiviteRepository;
import com.fesup.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activites")
@CrossOrigin(origins = "http://localhost:4200")
public class ActiviteController {
    
    @Autowired
    private ActiviteRepository activiteRepository;
    
    /**
     * Récupérer une activité par son ID
     * GET /api/activites/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActiviteDTO> getActivite(@PathVariable Long id) {
        Activite activite = activiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activité non trouvée"));
        
        ActiviteDTO dto = new ActiviteDTO(
            activite.getId(),
            activite.getTitre(),
            activite.getDescription(),
            activite.getType().toString(),
            activite.getDemiJournee().toString(),
            activite.getCapaciteMax()
        );
        
        return ResponseEntity.ok(dto);
    }
}
