package com.fesup.controller;

import com.fesup.dto.*;
import com.fesup.entity.Eleve;
import com.fesup.entity.Ticket;
import com.fesup.enums.StatutTicket;
import com.fesup.repository.EleveRepository;
import com.fesup.repository.TicketRepository;
import com.fesup.service.TicketStorageService;
import com.fesup.service.VoeuxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voeux")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class VoeuxController {
    
    private final VoeuxService voeuxService;
    private final EleveRepository eleveRepository;
    private final TicketRepository ticketRepository;
    private final TicketStorageService storageService;
    
    /**
     * Endpoint 1 : Vérification de l'identité de l'élève
     * POST /api/voeux/auth
     */
    @PostMapping("/auth")
    public ResponseEntity<AuthResponseDTO> verifierEleve(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = voeuxService.verifierEleve(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint 2 : Récupération des activités par type et demi-journée
     * GET /api/voeux/activites/{demiJournee}
     */
    @GetMapping("/activites/{demiJournee}")
    public ResponseEntity<Map<String, List<ActiviteDTO>>> getActivites(
            @PathVariable String demiJournee) {
        Map<String, List<ActiviteDTO>> activites = voeuxService.getActivitesParType(demiJournee);
        return ResponseEntity.ok(activites);
    }
    
    /**
     * Endpoint 3 : Soumission du formulaire de vœux
     * POST /api/voeux/soumettre
     */
    @PostMapping("/soumettre")
    public ResponseEntity<VoeuxSubmissionResponseDTO> soumettreVoeux(
            @Valid @RequestBody VoeuxSubmissionDTO submission) {
        VoeuxSubmissionResponseDTO response = voeuxService.soumettreVoeux(submission);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint 4 : Vérifier si élève a déjà soumis
     * GET /api/voeux/status/{eleveId}
     */
    @GetMapping("/status/{eleveId}")
    public ResponseEntity<Boolean> aDejasoumis(@PathVariable Long eleveId) {
        boolean dejasoumis = voeuxService.aDejasoumis(eleveId);
        return ResponseEntity.ok(dejasoumis);
    }
    
    /**
     * Endpoint 5 : Télécharger le ticket d'un élève (authentification ID/Nom)
     * GET /api/voeux/mon-ticket?eleveId={id}&nom={nom}
     */
    @GetMapping("/mon-ticket")
    public ResponseEntity<ByteArrayResource> telechargerMonTicket(
            @RequestParam Long eleveId,
            @RequestParam String nom) {
        
        // 1. Vérifier l'authentification (ID + Nom)
        Eleve eleve = eleveRepository.findById(eleveId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Élève introuvable"));
        
        if (!eleve.getNom().equalsIgnoreCase(nom.trim())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Nom incorrect");
        }
        
        // 2. Récupérer le ticket
        Ticket ticket = ticketRepository.findByEleve(eleve)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Aucun ticket généré. Contactez l'administration."));
        
        if (ticket.getStatut() != StatutTicket.GENERE) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Le ticket n'est pas encore disponible");
        }
        
        // 3. Lire le fichier PDF
        try {
            byte[] pdfData = storageService.recupererPdf(ticket.getCheminFichier());
            ByteArrayResource resource = new ByteArrayResource(pdfData);
            
            String filename = String.format("planning_%s_%s.pdf", 
                eleve.getPrenom().toLowerCase(), 
                eleve.getNom().toLowerCase());
            
            log.info("📄 Téléchargement ticket par élève {} ({})", 
                     eleve.getId(), eleve.getNom());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"")
                .contentLength(pdfData.length)
                .body(resource);
                
        } catch (IOException e) {
            log.error("Erreur lecture PDF pour élève {}: {}", eleveId, e.getMessage());
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erreur lors de la récupération du ticket");
        }
    }
    
    /**
     * Endpoint 6 : Vérifier la disponibilité d'un ticket
     * GET /api/voeux/mon-ticket/status?eleveId={id}&nom={nom}
     */
    @GetMapping("/mon-ticket/status")
    public ResponseEntity<Map<String, Boolean>> verifierDisponibiliteTicket(
            @RequestParam Long eleveId,
            @RequestParam String nom) {
        
        // Vérifier l'authentification
        Eleve eleve = eleveRepository.findById(eleveId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Élève introuvable"));
        
        if (!eleve.getNom().equalsIgnoreCase(nom.trim())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Nom incorrect");
        }
        
        // Vérifier si le ticket existe et est disponible
        boolean disponible = ticketRepository.findByEleve(eleve)
            .map(ticket -> ticket.getStatut() == StatutTicket.GENERE)
            .orElse(false);
        
        return ResponseEntity.ok(Map.of("disponible", disponible));
    }
}
