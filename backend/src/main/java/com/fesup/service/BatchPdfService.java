package com.fesup.service;

import com.fesup.entity.*;
import com.fesup.enums.StatutTicket;
import com.fesup.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchPdfService {
    
    private final EleveRepository eleveRepository;
    private final AffectationRepository affectationRepository;
    private final TicketRepository ticketRepository;
    private final PdfGenerationService pdfGenerationService;
    private final TicketStorageService storageService;
    
    /**
     * Génère tous les tickets en lot (asynchrone)
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<BatchResult> genererTousLesTickets() {
        log.info("🚀 Début de la génération batch des tickets...");
        
        long startTime = System.currentTimeMillis();
        BatchResult result = new BatchResult();
        
        // Récupérer tous les élèves ayant des affectations
        List<Eleve> eleves = eleveRepository.findAll();
        result.setTotalEleves(eleves.size());
        
        for (Eleve eleve : eleves) {
            try {
                genererTicketPourEleve(eleve);
                result.incrementSucces();
                
                if (result.getSucces() % 50 == 0) {
                    log.info("📊 Progression : {}/{} tickets générés", result.getSucces(), result.getTotalEleves());
                }
                
            } catch (Exception e) {
                log.error("❌ Erreur génération ticket pour élève {}: {}", eleve.getId(), e.getMessage(), e);
                result.incrementErreurs();
                result.addErreur(eleve.getId(), e.getMessage());
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        result.setDureeMs(duration);
        
        log.info("✅ Génération batch terminée : {} succès, {} erreurs en {} ms",
                 result.getSucces(), result.getErreurs(), duration);
        
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * Génère le ticket pour un élève spécifique
     */
    @Transactional
    public Ticket genererTicketPourEleve(Eleve eleve) throws Exception {
        // 1. Récupérer les affectations
        List<Affectation> affectations = affectationRepository.findByEleveAndAssignedSessionIsNotNull(eleve);
        
        if (affectations.isEmpty()) {
            throw new IllegalStateException("Aucune affectation pour cet élève");
        }
        
        // 2. Générer le PDF
        byte[] pdfData = pdfGenerationService.genererTicketEleve(eleve, affectations);
        
        // 3. Sauvegarder sur le file system
        String cheminFichier = storageService.sauvegarderPdf(eleve.getId(), pdfData);
        
        // 4. Enregistrer ou mettre à jour en BDD
        Ticket ticket = ticketRepository.findByEleve(eleve).orElse(new Ticket());
        
        ticket.setEleve(eleve);
        ticket.setCheminFichier(cheminFichier);
        ticket.setDateGeneration(LocalDateTime.now());
        ticket.setTailleFichier((long) pdfData.length);
        ticket.setStatut(StatutTicket.GENERE);
        
        return ticketRepository.save(ticket);
    }
    
    @Data
    public static class BatchResult {
        private int totalEleves;
        private int succes;
        private int erreurs;
        private long dureeMs;
        private Map<Long, String> erreursDetails = new HashMap<>();
        
        public void incrementSucces() { 
            succes++; 
        }
        
        public void incrementErreurs() { 
            erreurs++; 
        }
        
        public void addErreur(Long eleveId, String message) {
            erreursDetails.put(eleveId, message);
        }
    }
}
