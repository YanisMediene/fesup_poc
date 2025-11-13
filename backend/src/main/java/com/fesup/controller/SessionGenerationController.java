package com.fesup.controller;

import com.fesup.service.SessionGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour la génération automatique des sessions
 */
@RestController
@RequestMapping("/api/admin/sessions/generation")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SessionGenerationController {
    
    private final SessionGenerationService sessionGenerationService;
    
    /**
     * Génère automatiquement les sessions en fonction des vœux des élèves
     * 
     * @return Statistiques de génération
     */
    @PostMapping("/auto")
    public ResponseEntity<Map<String, Object>> genererSessionsAutomatiquement() {
        Map<String, Object> resultat = sessionGenerationService.genererSessionsAutomatiquement();
        return ResponseEntity.ok(resultat);
    }
}
