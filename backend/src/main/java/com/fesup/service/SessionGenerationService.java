package com.fesup.service;

import com.fesup.entity.*;
import com.fesup.enums.DemiJournee;
import com.fesup.enums.TypeActivite;
import com.fesup.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de génération automatique des sessions basées sur les vœux des élèves
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionGenerationService {
    
    private final VoeuRepository voeuRepository;
    private final SessionRepository sessionRepository;
    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;
    
    /**
     * Génère automatiquement les sessions en fonction des vœux des élèves
     * 
     * Logique :
     * 1. Analyse tous les vœux pour chaque activité
     * 2. Calcule le nombre de sessions nécessaires (demande / capacité activité)
     * 3. Crée les sessions en utilisant les salles et créneaux disponibles
     * 4. Priorise les activités les plus demandées
     */
    @Transactional
    public Map<String, Object> genererSessionsAutomatiquement() {
        log.info("🎯 Début de la génération automatique des sessions");
        
        // 1. Supprimer les anciennes sessions
        long oldCount = sessionRepository.count();
        sessionRepository.deleteAll();
        log.info("🗑️  {} anciennes sessions supprimées", oldCount);
        
        // 2. Récupérer tous les vœux
        List<Voeu> voeux = voeuRepository.findAllWithDetails();
        log.info("📊 {} vœux trouvés", voeux.size());
        
        // 3. Grouper les vœux par activité et demi-journée
        Map<DemiJournee, Map<Activite, List<Voeu>>> voeuxParDemiJourneeEtActivite = voeux.stream()
            .collect(Collectors.groupingBy(
                voeu -> voeu.getEleve().getDemiJournee(),
                Collectors.groupingBy(Voeu::getActivite)
            ));
        
        // 4. Statistiques de génération
        int totalSessionsCreees = 0;
        Map<String, Integer> statsParDemiJournee = new HashMap<>();
        
        // 5. Traiter chaque demi-journée séparément
        for (Map.Entry<DemiJournee, Map<Activite, List<Voeu>>> entryDJ : voeuxParDemiJourneeEtActivite.entrySet()) {
            DemiJournee demiJournee = entryDJ.getKey();
            Map<Activite, List<Voeu>> voeuxParActivite = entryDJ.getValue();
            
            log.info("🕐 Traitement de la demi-journée : {}", demiJournee);
            
            // Récupérer les salles et créneaux pour cette demi-journée
            List<Salle> salles = salleRepository.findAll();
            List<Creneau> creneaux = creneauRepository.findByDemiJournee(demiJournee);
            
            log.info("   📍 {} salles disponibles, {} créneaux disponibles", salles.size(), creneaux.size());
            
            // Trier les activités par nombre de vœux (décroissant)
            List<Map.Entry<Activite, List<Voeu>>> activitesTriees = voeuxParActivite.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());
            
            // Tracking des créneaux/salles utilisés pour éviter les conflits
            Map<String, Boolean> creneauSalleUtilises = new HashMap<>();
            int compteurCreneau = 0;
            int compteurSalle = 0;
            int sessionsCreesPourCetteDemiJournee = 0;
            
            // Créer les sessions
            for (Map.Entry<Activite, List<Voeu>> entry : activitesTriees) {
                Activite activite = entry.getKey();
                int nbVoeux = entry.getValue().size();
                
                // Pondération selon la priorité des vœux
                long nbVoeux1_2 = entry.getValue().stream()
                    .filter(v -> v.getPriorite() <= 2)
                    .count();
                
                // Calculer le nombre de sessions nécessaires
                int capacite = activite.getCapaciteMax();
                int nbSessionsNecessaires = (int) Math.ceil((double) nbVoeux1_2 / capacite);
                
                // Ajouter des sessions supplémentaires si beaucoup de vœux 3-4-5
                if (nbVoeux - nbVoeux1_2 > capacite) {
                    nbSessionsNecessaires += 1;
                }
                
                // 🎯 GARANTIE MINIMALE PAR TYPE D'ACTIVITÉ
                // Assurer qu'au moins 1 session est créée pour chaque type si des vœux existent
                int sessionsMinimales = calculerSessionsMinimales(activite.getType(), nbVoeux, nbVoeux1_2);
                if (nbSessionsNecessaires < sessionsMinimales) {
                    nbSessionsNecessaires = sessionsMinimales;
                    log.info("  ⭐ Garantie minimale appliquée pour {} (type: {}) : {} → {} sessions",
                        activite.getTitre(), activite.getType(), 
                        (int) Math.ceil((double) nbVoeux1_2 / capacite), sessionsMinimales);
                }
                
                // Limiter à 5 sessions max par activité (contrainte hard)
                nbSessionsNecessaires = Math.min(nbSessionsNecessaires, 5);
                
                log.info("  📌 {} : {} vœux (dont {} prioritaires) → {} sessions", 
                    activite.getTitre(), nbVoeux, nbVoeux1_2, nbSessionsNecessaires);
                
                // Créer les sessions en répartissant sur différents créneaux
                for (int i = 0; i < nbSessionsNecessaires; i++) {
                    // Trouver un créneau/salle disponible
                    boolean sessionCreee = false;
                    int tentatives = 0;
                    int maxTentatives = creneaux.size() * salles.size();
                    
                    while (!sessionCreee && tentatives < maxTentatives) {
                        Creneau creneauChoisi = creneaux.get(compteurCreneau % creneaux.size());
                        Salle salleChoisie = salles.stream()
                            .filter(s -> s.getCapacite() >= capacite)
                            .skip(compteurSalle % salles.size())
                            .findFirst()
                            .orElse(salles.get(0));
                        
                        String cle = creneauChoisi.getId() + "-" + salleChoisie.getId();
                        
                        if (!creneauSalleUtilises.getOrDefault(cle, false)) {
                            // Créneau/salle disponible, créer la session
                            Session session = new Session();
                            session.setActivite(activite);
                            session.setSalle(salleChoisie);
                            session.setCreneau(creneauChoisi);
                            
                            sessionRepository.save(session);
                            creneauSalleUtilises.put(cle, true);
                            
                            totalSessionsCreees++;
                            sessionsCreesPourCetteDemiJournee++;
                            sessionCreee = true;
                            
                            log.debug("     ✓ Session créée : {} dans {} à {}", 
                                activite.getTitre(), salleChoisie.getNom(), creneauChoisi.getLibelle());
                        }
                        
                        // Passer au créneau/salle suivant
                        compteurCreneau++;
                        if (compteurCreneau % creneaux.size() == 0) {
                            compteurSalle++;
                        }
                        tentatives++;
                    }
                    
                    if (!sessionCreee) {
                        log.warn("⚠️  Impossible de créer plus de sessions pour {} (plus de créneaux/salles disponibles)", 
                            activite.getTitre());
                        break;
                    }
                }
            }
            
            statsParDemiJournee.put(demiJournee.name(), sessionsCreesPourCetteDemiJournee);
            log.info("✅ {} sessions créées pour {}", sessionsCreesPourCetteDemiJournee, demiJournee);
        }
        
        // 6. Préparer le résumé
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("totalSessionsCreees", totalSessionsCreees);
        resultat.put("statsParDemiJournee", statsParDemiJournee);
        resultat.put("anciennesSessions", oldCount);
        
        log.info("✅ Génération terminée : {} sessions créées", totalSessionsCreees);
        
        return resultat;
    }
    
    /**
     * Calcule le nombre minimal de sessions à garantir selon le type d'activité
     * 
     * Logique de garantie minimale :
     * - CONFERENCE : Au moins 1 session si des vœux existent
     * - TABLE_RONDE : Au moins 1 session si ≥ 5 vœux (capacité généralement plus grande)
     * - FLASH_METIER : Au moins 1 session si ≥ 3 vœux (format court, capacité plus petite)
     * 
     * @param typeActivite Le type d'activité
     * @param nbVoeuxTotal Le nombre total de vœux pour cette activité
     * @param nbVoeuxPrioritaires Le nombre de vœux prioritaires (1 et 2)
     * @return Le nombre minimal de sessions à créer
     */
    private int calculerSessionsMinimales(TypeActivite typeActivite, int nbVoeuxTotal, long nbVoeuxPrioritaires) {
        if (nbVoeuxTotal == 0) {
            return 0; // Pas de vœux, pas de session
        }
        
        switch (typeActivite) {
            case CONFERENCE:
                // Pour les conférences, toujours au moins 1 session si des vœux existent
                return nbVoeuxTotal > 0 ? 1 : 0;
                
            case TABLE_RONDE:
                // Pour les tables rondes, au moins 1 session si au moins 5 vœux
                // (capacité généralement 40 personnes, mais on veut garantir la diversité)
                if (nbVoeuxTotal >= 5) {
                    return 1;
                }
                return 0;
                
            case FLASH_METIER:
                // Pour les flash métiers, au moins 1 session si au moins 3 vœux
                // (capacité généralement 15-20 personnes, format court)
                if (nbVoeuxTotal >= 3) {
                    return 1;
                }
                return 0;
                
            default:
                return 0;
        }
    }
}
