package com.fesup.controller;

import com.fesup.entity.Affectation;
import com.fesup.service.AffectationService;
import com.fesup.solver.AffectationSolution;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/affectations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AffectationController {
    
    private final AffectationService affectationService;
    
    /**
     * Lance l'algorithme d'affectation
     */
    @PostMapping("/lancer")
    public ResponseEntity<Map<String, Object>> lancerAffectation() {
        try {
            UUID problemId = affectationService.lancerAffectation();
            
            Map<String, Object> response = new HashMap<>();
            response.put("problemId", problemId.toString());
            response.put("status", "STARTED");
            response.put("message", "Algorithme lancé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Récupère le statut de l'affectation en cours
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("running", affectationService.isRunning());
        response.put("hasExistingResults", affectationService.hasExistingAffectations());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Récupère les résultats de l'affectation
     */
    @GetMapping("/resultats")
    public ResponseEntity<?> getResultats() {
        try {
            if (affectationService.isRunning()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "RUNNING");
                response.put("message", "Algorithme en cours d'exécution");
                return ResponseEntity.ok(response);
            }
            
            AffectationSolution solution = affectationService.getResultats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "COMPLETED");
            response.put("score", solution.getScore().toString());
            response.put("hardScore", solution.getScore().hardScore());
            response.put("softScore", solution.getScore().softScore());
            response.put("affectations", solution.getAffectations());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Récupère toutes les affectations sauvegardées
     */
    @GetMapping
    public ResponseEntity<List<Affectation>> getAllAffectations() {
        return ResponseEntity.ok(affectationService.getAllAffectations());
    }
    
    /**
     * Met à jour manuellement une affectation
     */
    @PutMapping("/{affectationId}")
    public ResponseEntity<?> updateAffectation(
            @PathVariable Long affectationId,
            @RequestParam Long sessionId) {
        try {
            Affectation updated = affectationService.updateAffectation(affectationId, sessionId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Supprime toutes les affectations et leurs PDFs associés
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllAffectations() {
        try {
            int count = affectationService.deleteAllAffectationsWithPdfs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Toutes les affectations et tickets PDF ont été supprimés avec succès");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
