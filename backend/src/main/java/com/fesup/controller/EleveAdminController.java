package com.fesup.controller;

import com.fesup.dto.CreateEleveDTO;
import com.fesup.entity.Eleve;
import com.fesup.entity.Lycee;
import com.fesup.entity.Voeu;
import com.fesup.enums.DemiJournee;
import com.fesup.exception.ResourceNotFoundException;
import com.fesup.exception.ValidationException;
import com.fesup.repository.EleveRepository;
import com.fesup.repository.LyceeRepository;
import com.fesup.repository.VoeuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/eleves")
@PreAuthorize("hasRole('ADMIN')")
public class EleveAdminController {
    
    @Autowired
    private EleveRepository eleveRepository;
    
    @Autowired
    private VoeuRepository voeuRepository;
    
    @Autowired
    private LyceeRepository lyceeRepository;
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllEleves() {
        List<Map<String, Object>> elevesWithDetails = eleveRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(elevesWithDetails);
    }
    
    /**
     * Crée un nouvel élève
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEleve(@RequestBody CreateEleveDTO createDTO) {
        // Validation
        if (createDTO.getNom() == null || createDTO.getNom().trim().isEmpty()) {
            throw new ValidationException("Le nom est obligatoire");
        }
        if (createDTO.getPrenom() == null || createDTO.getPrenom().trim().isEmpty()) {
            throw new ValidationException("Le prénom est obligatoire");
        }
        if (createDTO.getLyceeId() == null) {
            throw new ValidationException("Le lycée est obligatoire");
        }
        if (createDTO.getDemiJournee() == null || createDTO.getDemiJournee().trim().isEmpty()) {
            throw new ValidationException("La demi-journée est obligatoire");
        }
        
        // Vérifier que le lycée existe
        Lycee lycee = lyceeRepository.findById(createDTO.getLyceeId())
            .orElseThrow(() -> new ResourceNotFoundException("Lycée non trouvé"));
        
        // Valider la demi-journée
        DemiJournee demiJournee;
        try {
            demiJournee = DemiJournee.valueOf(createDTO.getDemiJournee().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Demi-journée invalide. Valeurs autorisées : JOUR1_MATIN, JOUR1_APRES_MIDI, JOUR2_MATIN, JOUR2_APRES_MIDI");
        }
        
        // Créer l'élève
        Eleve eleve = new Eleve();
        eleve.setNom(createDTO.getNom().toUpperCase().trim());
        eleve.setPrenom(createDTO.getPrenom().trim());
        eleve.setLycee(lycee);
        eleve.setDemiJournee(demiJournee);
        eleve.setVoeuxSoumis(false);
        
        Eleve savedEleve = eleveRepository.save(eleve);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedEleve));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEleveById(@PathVariable Long id) {
        return eleveRepository.findById(id)
            .map(eleve -> {
                Map<String, Object> details = convertToDTO(eleve);
                // Ajouter les vœux détaillés
                List<Voeu> voeux = voeuRepository.findByEleveId(id);
                List<Map<String, Object>> voeuxDetails = voeux.stream()
                    .map(voeu -> {
                        Map<String, Object> v = new HashMap<>();
                        v.put("id", voeu.getId());
                        v.put("priorite", voeu.getPriorite());
                        v.put("typeVoeu", voeu.getTypeVoeu().toString());
                        v.put("activite", Map.of(
                            "id", voeu.getActivite().getId(),
                            "titre", voeu.getActivite().getTitre(),
                            "type", voeu.getActivite().getType()
                        ));
                        return v;
                    })
                    .collect(Collectors.toList());
                details.put("voeux", voeuxDetails);
                return ResponseEntity.ok(details);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEleve(@PathVariable Long id) {
        if (eleveRepository.existsById(id)) {
            // Les vœux seront supprimés en cascade
            eleveRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/reset-voeux")
    public ResponseEntity<Map<String, Object>> resetVoeux(@PathVariable Long id) {
        return eleveRepository.findById(id)
            .map(eleve -> {
                // Supprimer tous les vœux de l'élève
                voeuRepository.deleteAll(voeuRepository.findByEleveId(id));
                
                // Réinitialiser le statut de soumission
                eleve.setVoeuxSoumis(false);
                Eleve updated = eleveRepository.save(eleve);
                
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalEleves = eleveRepository.count();
        long elevesAvecVoeux = eleveRepository.countByVoeuxSoumisTrue();
        long elevesSansVoeux = totalEleves - elevesAvecVoeux;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalEleves);
        stats.put("avecVoeux", elevesAvecVoeux);
        stats.put("sansVoeux", elevesSansVoeux);
        stats.put("tauxCompletion", totalEleves > 0 ? (elevesAvecVoeux * 100.0 / totalEleves) : 0);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Supprime tous les élèves et leurs vœux
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllEleves() {
        long eleveCount = eleveRepository.count();
        long voeuCount = voeuRepository.count();
        
        // Suppression explicite des vœux avant les élèves
        if (voeuCount > 0) {
            voeuRepository.deleteAll();
        }
        eleveRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tous les élèves ont été supprimés avec succès");
        response.put("elevesSupprimes", eleveCount);
        response.put("voeuxSupprimes", voeuCount);
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> convertToDTO(Eleve eleve) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", eleve.getId());
        dto.put("nom", eleve.getNom());
        dto.put("prenom", eleve.getPrenom());
        dto.put("idNational", eleve.getIdNational());
        dto.put("demiJournee", eleve.getDemiJournee().toString());
        dto.put("voeuxSoumis", eleve.isVoeuxSoumis());
        dto.put("dateSoumission", eleve.getDateSoumission());
        
        if (eleve.getLycee() != null) {
            Map<String, Object> lycee = new HashMap<>();
            lycee.put("id", eleve.getLycee().getId());
            lycee.put("nom", eleve.getLycee().getNom());
            lycee.put("ville", eleve.getLycee().getVille());
            lycee.put("codePostal", eleve.getLycee().getCodePostal());
            dto.put("lycee", lycee);
        }
        
        // Compter le nombre de vœux
        long nbVoeux = voeuRepository.countByEleveId(eleve.getId());
        dto.put("nbVoeux", nbVoeux);
        
        return dto;
    }
}
