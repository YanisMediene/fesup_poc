package com.fesup.service;

import com.fesup.dto.*;
import com.fesup.entity.Activite;
import com.fesup.entity.Eleve;
import com.fesup.entity.Voeu;
import com.fesup.enums.DemiJournee;
import com.fesup.enums.TypeActivite;
import com.fesup.enums.TypeVoeu;
import com.fesup.exception.AuthException;
import com.fesup.exception.BusinessException;
import com.fesup.exception.ResourceNotFoundException;
import com.fesup.exception.ValidationException;
import com.fesup.repository.ActiviteRepository;
import com.fesup.repository.EleveRepository;
import com.fesup.repository.VoeuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoeuxService {
    
    @Autowired
    private EleveRepository eleveRepository;
    
    @Autowired
    private ActiviteRepository activiteRepository;
    
    @Autowired
    private VoeuRepository voeuRepository;
    
    /**
     * Vérifie l'identité de l'élève
     */
    public AuthResponseDTO verifierEleve(AuthRequestDTO request) {
        Eleve eleve = eleveRepository.findByIdNationalAndNom(
            request.getIdNational(), 
            request.getNom().toUpperCase()
        ).orElseThrow(() -> new AuthException("ID National ou Nom incorrect"));
        
        return new AuthResponseDTO(
            eleve.getId(),
            eleve.getIdNational(),
            eleve.getNom(),
            eleve.getPrenom(),
            eleve.getLycee() != null ? eleve.getLycee().getNom() : "",
            eleve.getDemiJournee().name(),
            eleve.isVoeuxSoumis()
        );
    }
    
    /**
     * Récupère toutes les activités groupées par type
     */
    public Map<String, List<ActiviteDTO>> getActivitesParType(String demiJournee) {
        DemiJournee dj = DemiJournee.valueOf(demiJournee.toUpperCase());
        List<Activite> activites = activiteRepository.findByDemiJournee(dj);
        
        Map<String, List<ActiviteDTO>> grouped = new HashMap<>();
        grouped.put("conferences", new ArrayList<>());
        grouped.put("tablesRondes", new ArrayList<>());
        grouped.put("flashsMetiers", new ArrayList<>());
        
        for (Activite a : activites) {
            ActiviteDTO dto = new ActiviteDTO(
                a.getId(), 
                a.getTitre(), 
                a.getDescription(), 
                a.getType().toString(), 
                a.getDemiJournee().toString(),
                a.getCapaciteMax()
            );
            
            switch (a.getType()) {
                case CONFERENCE:
                    grouped.get("conferences").add(dto);
                    break;
                case TABLE_RONDE:
                    grouped.get("tablesRondes").add(dto);
                    break;
                case FLASH_METIER:
                    grouped.get("flashsMetiers").add(dto);
                    break;
            }
        }
        
        return grouped;
    }
    
    /**
     * LOGIQUE DE VALIDATION DES VŒUX 3-4-5 (PARTIE COMPLEXE)
     * Règles :
     * - Vœu 3 : 1 conférence parmi les 17 restantes OU 1 table ronde OU 1 flash métier
     * - Vœu 4 : 1 conférence parmi les 17 restantes OU 1 table ronde OU 1 flash métier
     * - Vœu 5 : 1 conférence parmi les 17 restantes OU 1 table ronde OU 1 flash métier
     * - Pas de doublons entre vœux 3-4-5
     * - Pas de doublons avec vœux 1 et 2
     */
    @Transactional
    public VoeuxSubmissionResponseDTO soumettreVoeux(VoeuxSubmissionDTO submission) {
        
        // 1. Vérifier que l'élève existe et n'a pas déjà soumis définitivement
        Eleve eleve = eleveRepository.findById(submission.getEleveId())
            .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé"));
        
        if (eleve.isVoeuxSoumis()) {
            throw new BusinessException("Vos vœux ont déjà été définitivement soumis et ne peuvent plus être modifiés");
        }
        
        // 1.5 SUPPRIMER LES ANCIENS VŒUX S'ILS EXISTENT (permet la modification)
        List<Voeu> anciensVoeux = voeuRepository.findByEleveOrderByPrioriteAsc(eleve);
        if (!anciensVoeux.isEmpty()) {
            voeuRepository.deleteAll(anciensVoeux);
        }
        
        // 2. Récupérer toutes les activités sélectionnées
        List<Long> activiteIds = Arrays.asList(
            submission.getConferenceVoeu1(),
            submission.getConferenceVoeu2(),
            submission.getActiviteVoeu3(),
            submission.getActiviteVoeu4(),
            submission.getActiviteVoeu5()
        );
        
        Map<Long, Activite> activitesMap = activiteRepository.findAllById(activiteIds)
            .stream()
            .collect(Collectors.toMap(Activite::getId, a -> a));
        
        // 3. VALIDATION VŒUX 1 ET 2 (doivent être des conférences)
        Activite conf1 = activitesMap.get(submission.getConferenceVoeu1());
        Activite conf2 = activitesMap.get(submission.getConferenceVoeu2());
        
        if (conf1 == null || conf2 == null) {
            throw new ValidationException("Vœux 1 et 2 obligatoires");
        }
        
        if (conf1.getType() != TypeActivite.CONFERENCE || 
            conf2.getType() != TypeActivite.CONFERENCE) {
            throw new ValidationException("Vœux 1 et 2 doivent être des conférences");
        }
        
        if (submission.getConferenceVoeu1().equals(submission.getConferenceVoeu2())) {
            throw new ValidationException("Vœux 1 et 2 doivent être différents");
        }
        
        // 4. VALIDATION VŒUX 3-4-5 (LOGIQUE COMPLEXE)
        List<Long> voeux345Ids = Arrays.asList(
            submission.getActiviteVoeu3(),
            submission.getActiviteVoeu4(),
            submission.getActiviteVoeu5()
        );
        
        // 4.1 Vérifier qu'il n'y a pas de doublons dans les vœux 3-4-5
        Set<Long> uniqueVoeux345 = new HashSet<>(voeux345Ids);
        if (uniqueVoeux345.size() != voeux345Ids.size()) {
            throw new ValidationException("Les vœux 3, 4 et 5 doivent être différents");
        }
        
        // 4.2 Vérifier qu'il n'y a pas de doublons avec les vœux 1 et 2
        Set<Long> voeux12 = new HashSet<>(Arrays.asList(
            submission.getConferenceVoeu1(),
            submission.getConferenceVoeu2()
        ));
        
        for (Long voeuId : voeux345Ids) {
            if (voeux12.contains(voeuId)) {
                throw new ValidationException(
                    "Les vœux 3-4-5 ne peuvent pas être identiques aux vœux 1 et 2"
                );
            }
        }
        
        // 4.3 Vérifier que les activités 3-4-5 sont valides
        for (Long voeuId : voeux345Ids) {
            Activite activite = activitesMap.get(voeuId);
            if (activite == null) {
                throw new ValidationException("Activité non trouvée : " + voeuId);
            }
            
            // Pour les vœux 3-4-5 : conférences (sauf les 2 déjà choisies), 
            // tables rondes ou flashs métiers
            if (activite.getType() != TypeActivite.CONFERENCE && 
                activite.getType() != TypeActivite.TABLE_RONDE && 
                activite.getType() != TypeActivite.FLASH_METIER) {
                throw new ValidationException("Type d'activité invalide pour vœux 3-4-5");
            }
        }
        
        // 4.4 Vérifier la demi-journée
        DemiJournee demiJourneeEleve = eleve.getDemiJournee();
        for (Activite activite : activitesMap.values()) {
            if (activite.getDemiJournee() != demiJourneeEleve) {
                throw new ValidationException(
                    "Toutes les activités doivent correspondre à votre demi-journée : " 
                    + demiJourneeEleve
                );
            }
        }
        
        // 5. ENREGISTREMENT DES VŒUX
        List<Voeu> voeux = new ArrayList<>();
        
        // Vœu 1
        voeux.add(creerVoeu(eleve, conf1, 1, TypeVoeu.VOEU_1_2));
        
        // Vœu 2
        voeux.add(creerVoeu(eleve, conf2, 2, TypeVoeu.VOEU_1_2));
        
        // Vœux 3-4-5
        voeux.add(creerVoeu(eleve, activitesMap.get(submission.getActiviteVoeu3()), 
                           3, TypeVoeu.VOEU_3_4_5));
        voeux.add(creerVoeu(eleve, activitesMap.get(submission.getActiviteVoeu4()), 
                           4, TypeVoeu.VOEU_3_4_5));
        voeux.add(creerVoeu(eleve, activitesMap.get(submission.getActiviteVoeu5()), 
                           5, TypeVoeu.VOEU_3_4_5));
        
        voeuRepository.saveAll(voeux);
        
        // NE PAS marquer l'élève comme ayant soumis ici
        // Cela sera fait lors de la validation finale dans le récapitulatif
        
        return new VoeuxSubmissionResponseDTO(
            true, 
            "Vœux enregistrés avec succès", 
            null
        );
    }
    
    /**
     * Méthode utilitaire pour créer un vœu
     */
    private Voeu creerVoeu(Eleve eleve, Activite activite, int priorite, TypeVoeu typeVoeu) {
        Voeu voeu = new Voeu();
        voeu.setEleve(eleve);
        voeu.setActivite(activite);
        voeu.setPriorite(priorite);
        voeu.setTypeVoeu(typeVoeu);
        return voeu;
    }
    
    /**
     * Vérifie si un élève a déjà soumis ses vœux
     */
    public boolean aDejasoumis(Long eleveId) {
        return eleveRepository.findById(eleveId)
            .map(Eleve::isVoeuxSoumis)
            .orElse(false);
    }
    
    /**
     * Récupère les vœux d'un élève
     */
    public List<VoeuDTO> getVoeuxByEleve(Long eleveId) {
        Eleve eleve = eleveRepository.findById(eleveId)
            .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé"));
        
        List<Voeu> voeux = voeuRepository.findByEleveOrderByPrioriteAsc(eleve);
        
        return voeux.stream()
            .map(voeu -> new VoeuDTO(
                voeu.getId(),
                voeu.getEleve().getId(),
                voeu.getActivite().getId(),
                voeu.getPriorite()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Valide définitivement les vœux d'un élève
     */
    public void validerVoeux(Long eleveId) {
        Eleve eleve = eleveRepository.findById(eleveId)
            .orElseThrow(() -> new ResourceNotFoundException("Élève non trouvé"));
        
        // Vérifier qu'il a bien des vœux enregistrés
        List<Voeu> voeux = voeuRepository.findByEleveOrderByPrioriteAsc(eleve);
        if (voeux.isEmpty() || voeux.size() != 5) {
            throw new BusinessException("Vous devez avoir exactement 5 vœux enregistrés");
        }
        
        // Marquer comme définitivement soumis
        eleve.setVoeuxSoumis(true);
        eleve.setDateSoumission(LocalDateTime.now());
        eleveRepository.save(eleve);
    }
}
