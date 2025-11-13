package com.fesup.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.fesup.entity.Affectation;
import com.fesup.entity.Eleve;
import com.fesup.entity.Session;
import com.fesup.entity.Voeu;
import com.fesup.repository.AffectationRepository;
import com.fesup.repository.EleveRepository;
import com.fesup.repository.SessionRepository;
import com.fesup.repository.VoeuRepository;
import com.fesup.repository.TicketRepository;
import com.fesup.solver.AffectationSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AffectationService {
    
    private final SolverManager<AffectationSolution, UUID> solverManager;
    private final AffectationRepository affectationRepository;
    private final EleveRepository eleveRepository;
    private final SessionRepository sessionRepository;
    private final VoeuRepository voeuRepository;
    private final TicketRepository ticketRepository;
    private final TicketStorageService ticketStorageService;
    
    private UUID currentProblemId;
    private AffectationSolution lastSolution;
    
    /**
     * Lance l'algorithme d'affectation Timefold
     */
    @Transactional(readOnly = true)
    public UUID lancerAffectation() {
        // Réinitialiser la solution précédente
        this.lastSolution = null;
        
        // 1. Charger les données avec EAGER LOADING pour éviter LazyInitializationException
        List<Eleve> eleves = eleveRepository.findAllWithLycee();
        List<Session> sessions = sessionRepository.findAllWithDetails();
        List<Voeu> voeux = voeuRepository.findAllWithDetails();
        
        // ✅ VALIDATION : Vérifier qu'il y a des sessions disponibles
        if (sessions == null || sessions.isEmpty()) {
            throw new IllegalStateException("Impossible de lancer l'algorithme : aucune session n'a été créée. Veuillez d'abord créer des sessions.");
        }
        
        if (eleves == null || eleves.isEmpty()) {
            throw new IllegalStateException("Impossible de lancer l'algorithme : aucun élève trouvé dans la base de données.");
        }
        
        log.info("🚀 Lancement de l'algorithme - {} élèves, {} sessions, {} vœux", 
                 eleves.size(), sessions.size(), voeux.size());
        
        // 2. Créer 4 affectations par élève (une par créneau de sa demi-journée)
        List<Affectation> affectations = new ArrayList<>();
        for (Eleve eleve : eleves) {
            // Créer 4 affectations pour que l'élève puisse avoir jusqu'à 4 sessions
            for (int i = 0; i < 4; i++) {
                affectations.add(new Affectation(eleve, null));
            }
        }
        
        // 3. Créer le problème
        AffectationSolution problem = new AffectationSolution(voeux, sessions, affectations);
        
        // 4. Lancer le solver de manière asynchrone
        currentProblemId = UUID.randomUUID();
        solverManager.solveAndListen(
            currentProblemId,
            problemId -> problem,
            this::sauvegarderSolution
        );
        
        return currentProblemId;
    }
    
    /**
     * Récupère les résultats de l'affectation
     */
    public AffectationSolution getResultats() {
        if (currentProblemId == null) {
            throw new IllegalStateException("Aucune affectation en cours");
        }
        
        if (lastSolution != null) {
            return lastSolution;
        }
        
        throw new IllegalStateException("Solution pas encore disponible");
    }
    
    /**
     * Vérifie si le solver est encore en cours d'exécution
     */
    public boolean isRunning() {
        return lastSolution == null && currentProblemId != null;
    }
    
    /**
     * Vérifie si des affectations ont déjà été sauvegardées
     */
    public boolean hasExistingAffectations() {
        return affectationRepository.count() > 0;
    }
    
    /**
     * Sauvegarde la solution en base de données
     */
    @Transactional
    protected void sauvegarderSolution(AffectationSolution solution) {
        log.info("✅ Callback sauvegarderSolution appelé - Score: {}", solution.getScore());
        
        // Mettre à jour la solution courante
        this.lastSolution = solution;
        
        // Supprimer les anciennes affectations
        long oldCount = affectationRepository.count();
        affectationRepository.deleteAll();
        log.info("🗑️  {} anciennes affectations supprimées", oldCount);
        
        // Sauvegarder les nouvelles affectations
        int saved = 0;
        for (Affectation affectation : solution.getAffectations()) {
            if (affectation.getAssignedSession() != null) {
                affectationRepository.save(affectation);
                saved++;
            }
        }
        log.info("💾 {} nouvelles affectations sauvegardées", saved);
    }
    
    /**
     * Récupère toutes les affectations sauvegardées
     */
    public List<Affectation> getAllAffectations() {
        return affectationRepository.findAllWithSession();
    }
    
    /**
     * Met à jour manuellement une affectation
     */
    @Transactional
    public Affectation updateAffectation(Long affectationId, Long sessionId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable"));
        
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));
        
        affectation.setAssignedSession(session);
        return affectationRepository.save(affectation);
    }
    
    /**
     * Supprime toutes les affectations et les PDFs associés
     */
    @Transactional
    public int deleteAllAffectationsWithPdfs() {
        try {
            // 1. Compter avant suppression
            long affectationCount = affectationRepository.count();
            long ticketCount = ticketRepository.count();
            
            // 2. Supprimer tous les PDFs du filesystem
            int pdfCount = ticketStorageService.supprimerTousPdfs();
            
            // 3. Supprimer les tickets de la base
            ticketRepository.deleteAll();
            
            // 4. Supprimer les affectations
            affectationRepository.deleteAll();
            
            log.info("🗑️  Suppression complète : {} affectations, {} tickets, {} PDFs", 
                     affectationCount, ticketCount, pdfCount);
            
            return (int) affectationCount;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des affectations et PDFs", e);
            throw new RuntimeException("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}
