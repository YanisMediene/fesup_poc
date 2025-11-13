package com.fesup.service;

import com.fesup.dto.ImportErrorDTO;
import com.fesup.dto.ImportReportDTO;
import com.fesup.entity.Activite;
import com.fesup.entity.Eleve;
import com.fesup.entity.Lycee;
import com.fesup.entity.Salle;
import com.fesup.enums.DemiJournee;
import com.fesup.enums.TypeActivite;
import com.fesup.exception.BusinessException;
import com.fesup.repository.ActiviteRepository;
import com.fesup.repository.EleveRepository;
import com.fesup.repository.LyceeRepository;
import com.fesup.repository.SalleRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
public class AdminImportService {
    
    @Autowired
    private EleveRepository eleveRepository;
    
    @Autowired
    private LyceeRepository lyceeRepository;
    
    @Autowired
    private ActiviteRepository activiteRepository;
    
    @Autowired
    private SalleRepository salleRepository;
    
    @Transactional
    public ImportReportDTO importEleves(MultipartFile file) {
        ImportReportDTO report = new ImportReportDTO();
        
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            
            List<String[]> rows = csvReader.readAll();
            report.setTotalLines(rows.size());
            
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    // Format CSV: nom,prenom,idNational,lycee,ville,codePostal,demiJournee
                    if (row.length < 7) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "Nombre de colonnes insuffisant (attendu: nom,prenom,idNational,lycee,ville,codePostal,demiJournee)", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    String nom = row[0].trim().toUpperCase();
                    String prenom = row[1].trim();
                    String idNational = row[2].trim().toUpperCase();
                    String lyceeNom = row[3].trim();
                    String ville = row[4].trim();
                    String codePostal = row[5].trim();
                    String demiJourneeStr = row[6].trim().toUpperCase();
                    
                    // Valider idNational
                    if (idNational.isEmpty() || !idNational.matches("^[A-Z0-9]{5,50}$")) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "ID National invalide (alphanumérique 5-50 caractères)", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    // Vérifier unicité idNational
                    if (eleveRepository.findByIdNational(idNational).isPresent()) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "ID National déjà existant: " + idNational, String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    // Valider code postal
                    if (!codePostal.matches("\\d{5}")) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "Code postal invalide (doit être 5 chiffres)", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    // Valider demiJournee
                    DemiJournee demiJournee;
                    try {
                        demiJournee = DemiJournee.valueOf(demiJourneeStr);
                    } catch (IllegalArgumentException e) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "DemiJournee invalide (MATIN ou APRES_MIDI)", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    // Auto-créer ou mettre à jour le lycée
                    Lycee lycee = lyceeRepository.findByNom(lyceeNom)
                        .orElseGet(() -> {
                            Lycee newLycee = new Lycee();
                            newLycee.setNom(lyceeNom);
                            newLycee.setVille(ville);
                            newLycee.setCodePostal(codePostal);
                            return lyceeRepository.save(newLycee);
                        });
                    
                    // Mettre à jour ville et code postal si le lycée existe déjà mais sans ces infos
                    if (lycee.getVille() == null || lycee.getVille().isEmpty()) {
                        lycee.setVille(ville);
                        lycee.setCodePostal(codePostal);
                        lyceeRepository.save(lycee);
                    }
                    
                    // Créer l'élève
                    Eleve eleve = new Eleve();
                    eleve.setNom(nom);
                    eleve.setPrenom(prenom);
                    eleve.setIdNational(idNational);
                    eleve.setLycee(lycee);
                    eleve.setDemiJournee(demiJournee);
                    eleve.setVoeuxSoumis(false);
                    
                    eleveRepository.save(eleve);
                    report.setSuccessCount(report.getSuccessCount() + 1);
                    
                } catch (Exception e) {
                    report.getErrors().add(new ImportErrorDTO(i + 2, 
                        e.getMessage(), String.join(",", row)));
                    report.setErrorCount(report.getErrorCount() + 1);
                }
            }
            
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        
        return report;
    }
    
    @Transactional
    public ImportReportDTO importActivites(MultipartFile file) {
        ImportReportDTO report = new ImportReportDTO();
        
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            
            List<String[]> rows = csvReader.readAll();
            report.setTotalLines(rows.size());
            
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    // Format CSV: titre,description,type,demiJournee,capaciteMax
                    if (row.length < 5) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "Nombre de colonnes insuffisant", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    String titre = row[0].trim();
                    String description = row[1].trim();
                    String typeStr = row[2].trim().toUpperCase();
                    String demiJourneeStr = row[3].trim().toUpperCase();
                    Integer capaciteMax = Integer.parseInt(row[4].trim());
                    
                    // Valider type
                    TypeActivite type;
                    try {
                        type = TypeActivite.valueOf(typeStr);
                    } catch (IllegalArgumentException e) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "Type invalide (CONFERENCE, TABLE_RONDE, FLASH_METIER)", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    // Valider demiJournee
                    DemiJournee demiJournee;
                    try {
                        demiJournee = DemiJournee.valueOf(demiJourneeStr);
                    } catch (IllegalArgumentException e) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "DemiJournee invalide (MATIN ou APRES_MIDI)", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    // Créer l'activité
                    Activite activite = new Activite();
                    activite.setTitre(titre);
                    activite.setDescription(description);
                    activite.setType(type);
                    activite.setDemiJournee(demiJournee);
                    activite.setCapaciteMax(capaciteMax);
                    
                    activiteRepository.save(activite);
                    report.setSuccessCount(report.getSuccessCount() + 1);
                    
                } catch (NumberFormatException e) {
                    report.getErrors().add(new ImportErrorDTO(i + 2, 
                        "Capacité invalide (doit être un nombre)", String.join(",", row)));
                    report.setErrorCount(report.getErrorCount() + 1);
                } catch (Exception e) {
                    report.getErrors().add(new ImportErrorDTO(i + 2, 
                        e.getMessage(), String.join(",", row)));
                    report.setErrorCount(report.getErrorCount() + 1);
                }
            }
            
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        
        return report;
    }
    
    @Transactional
    public ImportReportDTO importSalles(MultipartFile file) {
        ImportReportDTO report = new ImportReportDTO();
        
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            
            List<String[]> rows = csvReader.readAll();
            report.setTotalLines(rows.size());
            
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    // Format CSV: nom,capacite,batiment,equipements
                    if (row.length < 4) {
                        report.getErrors().add(new ImportErrorDTO(i + 2, 
                            "Nombre de colonnes insuffisant", String.join(",", row)));
                        report.setErrorCount(report.getErrorCount() + 1);
                        continue;
                    }
                    
                    String nom = row[0].trim();
                    Integer capacite = Integer.parseInt(row[1].trim());
                    String batiment = row[2].trim();
                    String equipements = row[3].trim();
                    
                    // Créer la salle
                    Salle salle = new Salle();
                    salle.setNom(nom);
                    salle.setCapacite(capacite);
                    salle.setBatiment(batiment);
                    salle.setEquipements(equipements);
                    
                    salleRepository.save(salle);
                    report.setSuccessCount(report.getSuccessCount() + 1);
                    
                } catch (NumberFormatException e) {
                    report.getErrors().add(new ImportErrorDTO(i + 2, 
                        "Capacité invalide (doit être un nombre)", String.join(",", row)));
                    report.setErrorCount(report.getErrorCount() + 1);
                } catch (Exception e) {
                    report.getErrors().add(new ImportErrorDTO(i + 2, 
                        e.getMessage(), String.join(",", row)));
                    report.setErrorCount(report.getErrorCount() + 1);
                }
            }
            
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        
        return report;
    }
}
