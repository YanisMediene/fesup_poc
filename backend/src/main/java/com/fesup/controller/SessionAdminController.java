package com.fesup.controller;

import com.fesup.dto.SessionDTO;
import com.fesup.entity.*;
import com.fesup.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SessionAdminController {

    private final SessionRepository sessionRepository;
    private final ActiviteRepository activiteRepository;
    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;
    private final AffectationRepository affectationRepository;

    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        List<SessionDTO> dtos = sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        return sessionRepository.findById(id)
                .map(session -> ResponseEntity.ok(convertToDTO(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody SessionDTO sessionDTO) {
        // Validation: vérifier que activité, salle et créneau existent
        Activite activite = activiteRepository.findById(sessionDTO.getActiviteId())
                .orElse(null);
        if (activite == null) {
            return ResponseEntity.badRequest().body("Activité non trouvée");
        }

        Salle salle = salleRepository.findById(sessionDTO.getSalleId())
                .orElse(null);
        if (salle == null) {
            return ResponseEntity.badRequest().body("Salle non trouvée");
        }

        Creneau creneau = creneauRepository.findById(sessionDTO.getCreneauId())
                .orElse(null);
        if (creneau == null) {
            return ResponseEntity.badRequest().body("Créneau non trouvé");
        }

        // Créer la session
        Session session = new Session();
        session.setActivite(activite);
        session.setSalle(salle);
        session.setCreneau(creneau);
        // capaciteDisponible sera calculée automatiquement via @PrePersist

        Session saved = sessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody SessionDTO sessionDTO) {
        return sessionRepository.findById(id)
                .map(session -> {
                    // Validation et mise à jour
                    Activite activite = activiteRepository.findById(sessionDTO.getActiviteId())
                            .orElse(null);
                    if (activite == null) {
                        return ResponseEntity.badRequest().body("Activité non trouvée");
                    }

                    Salle salle = salleRepository.findById(sessionDTO.getSalleId())
                            .orElse(null);
                    if (salle == null) {
                        return ResponseEntity.badRequest().body("Salle non trouvée");
                    }

                    Creneau creneau = creneauRepository.findById(sessionDTO.getCreneauId())
                            .orElse(null);
                    if (creneau == null) {
                        return ResponseEntity.badRequest().body("Créneau non trouvé");
                    }

                    session.setActivite(activite);
                    session.setSalle(salle);
                    session.setCreneau(creneau);
                    // capaciteDisponible sera recalculée via @PreUpdate

                    Session updated = sessionRepository.save(session);
                    return ResponseEntity.ok(convertToDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        if (!sessionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sessionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Supprime toutes les sessions
     * Supprime d'abord les affectations qui dépendent des sessions
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllSessions() {
        long sessionCount = sessionRepository.count();
        long affectationCount = affectationRepository.count();
        
        // Supprimer d'abord les affectations pour éviter les violations de contraintes FK
        if (affectationCount > 0) {
            affectationRepository.deleteAll();
        }
        
        // Ensuite supprimer les sessions
        sessionRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Toutes les sessions ont été supprimées avec succès");
        response.put("sessionsSupprimes", sessionCount);
        response.put("affectationsSupprimes", affectationCount);
        
        return ResponseEntity.ok(response);
    }

    private SessionDTO convertToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setActiviteId(session.getActivite().getId());
        dto.setActiviteTitre(session.getActivite().getTitre());
        dto.setActiviteType(session.getActivite().getType().name());
        dto.setSalleId(session.getSalle().getId());
        dto.setSalleNom(session.getSalle().getNom());
        dto.setSalleCapacite(session.getSalle().getCapacite());
        dto.setCreneauId(session.getCreneau().getId());
        dto.setCreneauLibelle(session.getCreneau().getLibelle());
        dto.setCreneauDemiJournee(session.getCreneau().getDemiJournee().name());
        dto.setCapaciteDisponible(session.getCapaciteDisponible());
        return dto;
    }
}
