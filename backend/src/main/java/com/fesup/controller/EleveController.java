package com.fesup.controller;

import com.fesup.dto.VoeuDTO;
import com.fesup.service.VoeuxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eleves")
@CrossOrigin(origins = "http://localhost:4200")
public class EleveController {
    
    @Autowired
    private VoeuxService voeuxService;
    
    /**
     * Récupérer les vœux d'un élève
     * GET /api/eleves/{eleveId}/voeux
     */
    @GetMapping("/{eleveId}/voeux")
    public ResponseEntity<List<VoeuDTO>> getVoeuxByEleve(@PathVariable Long eleveId) {
        List<VoeuDTO> voeux = voeuxService.getVoeuxByEleve(eleveId);
        return ResponseEntity.ok(voeux);
    }
    
    /**
     * Valider définitivement les vœux d'un élève
     * POST /api/eleves/{eleveId}/valider-voeux
     */
    @PostMapping("/{eleveId}/valider-voeux")
    public ResponseEntity<Void> validerVoeux(@PathVariable Long eleveId) {
        voeuxService.validerVoeux(eleveId);
        return ResponseEntity.ok().build();
    }
}
