package com.fesup.controller;

import com.fesup.entity.Eleve;
import com.fesup.entity.Ticket;
import com.fesup.repository.EleveRepository;
import com.fesup.repository.TicketRepository;
import com.fesup.service.BatchPdfService;
import com.fesup.service.TicketStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@Slf4j
public class AdminTicketController {
    
    private final BatchPdfService batchPdfService;
    private final EleveRepository eleveRepository;
    private final TicketRepository ticketRepository;
    private final TicketStorageService storageService;
    
    /**
     * Déclenche la génération de tous les tickets (asynchrone)
     */
    @PostMapping("/generer-tous")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> genererTousLesTickets() {
        log.info("🎯 Admin a déclenché la génération batch des tickets");
        
        // Lancer le batch en asynchrone
        batchPdfService.genererTousLesTickets()
            .thenAccept(result -> {
                log.info("✅ Batch terminé : {} succès / {} erreurs", 
                         result.getSucces(), result.getErreurs());
            });
        
        return ResponseEntity.accepted().body(Map.of(
            "message", "Génération des tickets lancée en arrière-plan",
            "status", "EN_COURS"
        ));
    }
    
    /**
     * Régénère le ticket d'un élève spécifique
     */
    @PostMapping("/eleves/{id}/regenerer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> regenererTicket(@PathVariable Long id) {
        try {
            Eleve eleve = eleveRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Élève introuvable"));
            
            Ticket ticket = batchPdfService.genererTicketPourEleve(eleve);
            
            return ResponseEntity.ok(Map.of(
                "message", "Ticket régénéré avec succès",
                "ticketId", ticket.getId(),
                "dateGeneration", ticket.getDateGeneration()
            ));
            
        } catch (Exception e) {
            log.error("Erreur régénération ticket élève {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Télécharger le ticket d'un élève (admin)
     */
    @GetMapping("/eleves/{id}/ticket")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> telechargerTicketEleve(@PathVariable Long id) {
        
        // 1. Récupérer l'élève
        Eleve eleve = eleveRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Élève introuvable"));
        
        // 2. Récupérer le ticket
        Ticket ticket = ticketRepository.findByEleve(eleve)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket non généré"));
        
        // 3. Lire le fichier
        try {
            byte[] pdfData = storageService.recupererPdf(ticket.getCheminFichier());
            ByteArrayResource resource = new ByteArrayResource(pdfData);
            
            String filename = String.format("ticket_%s_%s.pdf", 
                eleve.getPrenom(), eleve.getNom());
            
            log.info("📄 Téléchargement ticket élève {} par admin", eleve.getId());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"")
                .contentLength(pdfData.length)
                .body(resource);
                
        } catch (IOException e) {
            log.error("Erreur lecture PDF: {}", e.getMessage());
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erreur récupération ticket");
        }
    }
}
