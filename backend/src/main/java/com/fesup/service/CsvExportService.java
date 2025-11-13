package com.fesup.service;

import com.fesup.entity.*;
import com.fesup.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service d'export CSV pour toutes les entités
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CsvExportService {
    
    private final EleveRepository eleveRepository;
    private final ActiviteRepository activiteRepository;
    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;
    private final LyceeRepository lyceeRepository;
    private final VoeuRepository voeuRepository;
    private final SessionRepository sessionRepository;
    private final AffectationRepository affectationRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Génère un fichier ZIP contenant tous les exports CSV
     */
    public byte[] exporterToutesLesDonnees() throws IOException {
        log.info("📦 Début de l'export complet en ZIP");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Ajouter chaque export CSV dans le ZIP
            ajouterCsvAuZip(zos, "eleves.csv", exporterEleves());
            ajouterCsvAuZip(zos, "activites.csv", exporterActivites());
            ajouterCsvAuZip(zos, "salles.csv", exporterSalles());
            ajouterCsvAuZip(zos, "creneaux.csv", exporterCreneaux());
            ajouterCsvAuZip(zos, "lycees.csv", exporterLycees());
            ajouterCsvAuZip(zos, "voeux.csv", exporterVoeux());
            ajouterCsvAuZip(zos, "sessions.csv", exporterSessions());
            ajouterCsvAuZip(zos, "affectations.csv", exporterAffectations());
        }
        
        log.info("✅ Export ZIP terminé : {} octets", baos.size());
        return baos.toByteArray();
    }
    
    /**
     * Ajoute un CSV au ZIP
     */
    private void ajouterCsvAuZip(ZipOutputStream zos, String nomFichier, String contenuCsv) throws IOException {
        ZipEntry entry = new ZipEntry(nomFichier);
        zos.putNextEntry(entry);
        zos.write(contenuCsv.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
    
    /**
     * Exporte les élèves en CSV
     */
    public String exporterEleves() {
        log.info("📊 Export des élèves...");
        List<Eleve> eleves = eleveRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nom,Prenom,Lycee,Ville,CodePostal,DemiJournee,VoeuxSoumis,DateSoumission\n");
        
        for (Eleve eleve : eleves) {
            csv.append(eleve.getId()).append(",")
               .append(escapeCsv(eleve.getNom())).append(",")
               .append(escapeCsv(eleve.getPrenom())).append(",")
               .append(escapeCsv(eleve.getLycee().getNom())).append(",")
               .append(escapeCsv(eleve.getLycee().getVille())).append(",")
               .append(escapeCsv(eleve.getLycee().getCodePostal())).append(",")
               .append(eleve.getDemiJournee()).append(",")
               .append(eleve.isVoeuxSoumis() ? "Oui" : "Non").append(",")
               .append(eleve.getDateSoumission() != null ? eleve.getDateSoumission().format(DATE_FORMATTER) : "")
               .append("\n");
        }
        
        log.info("✅ {} élèves exportés", eleves.size());
        return csv.toString();
    }
    
    /**
     * Exporte les activités en CSV
     */
    public String exporterActivites() {
        log.info("📊 Export des activités...");
        List<Activite> activites = activiteRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Titre,Description,Type,DemiJournee,CapaciteMax\n");
        
        for (Activite activite : activites) {
            csv.append(activite.getId()).append(",")
               .append(escapeCsv(activite.getTitre())).append(",")
               .append(escapeCsv(activite.getDescription())).append(",")
               .append(activite.getType()).append(",")
               .append(activite.getDemiJournee()).append(",")
               .append(activite.getCapaciteMax())
               .append("\n");
        }
        
        log.info("✅ {} activités exportées", activites.size());
        return csv.toString();
    }
    
    /**
     * Exporte les salles en CSV
     */
    public String exporterSalles() {
        log.info("📊 Export des salles...");
        List<Salle> salles = salleRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nom,Capacite,Batiment,Equipements\n");
        
        for (Salle salle : salles) {
            csv.append(salle.getId()).append(",")
               .append(escapeCsv(salle.getNom())).append(",")
               .append(salle.getCapacite()).append(",")
               .append(escapeCsv(salle.getBatiment())).append(",")
               .append(escapeCsv(salle.getEquipements()))
               .append("\n");
        }
        
        log.info("✅ {} salles exportées", salles.size());
        return csv.toString();
    }
    
    /**
     * Exporte les créneaux en CSV
     */
    public String exporterCreneaux() {
        log.info("📊 Export des créneaux...");
        List<Creneau> creneaux = creneauRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Libelle,HeureDebut,HeureFin,DemiJournee\n");
        
        for (Creneau creneau : creneaux) {
            csv.append(creneau.getId()).append(",")
               .append(escapeCsv(creneau.getLibelle())).append(",")
               .append(creneau.getHeureDebut()).append(",")
               .append(creneau.getHeureFin()).append(",")
               .append(creneau.getDemiJournee())
               .append("\n");
        }
        
        log.info("✅ {} créneaux exportés", creneaux.size());
        return csv.toString();
    }
    
    /**
     * Exporte les lycées en CSV
     */
    public String exporterLycees() {
        log.info("📊 Export des lycées...");
        List<Lycee> lycees = lyceeRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nom,Ville,CodePostal\n");
        
        for (Lycee lycee : lycees) {
            csv.append(lycee.getId()).append(",")
               .append(escapeCsv(lycee.getNom())).append(",")
               .append(escapeCsv(lycee.getVille())).append(",")
               .append(escapeCsv(lycee.getCodePostal()))
               .append("\n");
        }
        
        log.info("✅ {} lycées exportés", lycees.size());
        return csv.toString();
    }
    
    /**
     * Exporte les vœux en CSV
     */
    public String exporterVoeux() {
        log.info("📊 Export des vœux...");
        List<Voeu> voeux = voeuRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,EleveID,EleveNom,ElevePrenom,ActiviteID,ActiviteTitre,Priorite,TypeVoeu\n");
        
        for (Voeu voeu : voeux) {
            csv.append(voeu.getId()).append(",")
               .append(voeu.getEleve().getId()).append(",")
               .append(escapeCsv(voeu.getEleve().getNom())).append(",")
               .append(escapeCsv(voeu.getEleve().getPrenom())).append(",")
               .append(voeu.getActivite().getId()).append(",")
               .append(escapeCsv(voeu.getActivite().getTitre())).append(",")
               .append(voeu.getPriorite()).append(",")
               .append(voeu.getTypeVoeu())
               .append("\n");
        }
        
        log.info("✅ {} vœux exportés", voeux.size());
        return csv.toString();
    }
    
    /**
     * Exporte les sessions en CSV
     */
    public String exporterSessions() {
        log.info("📊 Export des sessions...");
        List<Session> sessions = sessionRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,ActiviteID,ActiviteTitre,SalleID,SalleNom,CreneauID,CreneauLibelle\n");
        
        for (Session session : sessions) {
            csv.append(session.getId()).append(",")
               .append(session.getActivite().getId()).append(",")
               .append(escapeCsv(session.getActivite().getTitre())).append(",")
               .append(session.getSalle().getId()).append(",")
               .append(escapeCsv(session.getSalle().getNom())).append(",")
               .append(session.getCreneau().getId()).append(",")
               .append(escapeCsv(session.getCreneau().getLibelle()))
               .append("\n");
        }
        
        log.info("✅ {} sessions exportées", sessions.size());
        return csv.toString();
    }
    
    /**
     * Exporte les affectations en CSV
     */
    public String exporterAffectations() {
        log.info("📊 Export des affectations...");
        List<Affectation> affectations = affectationRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,EleveID,EleveNom,ElevePrenom,SessionID,ActiviteTitre,SalleNom,CreneauLibelle\n");
        
        for (Affectation affectation : affectations) {
            Session session = affectation.getAssignedSession();
            csv.append(affectation.getId()).append(",")
               .append(affectation.getEleve().getId()).append(",")
               .append(escapeCsv(affectation.getEleve().getNom())).append(",")
               .append(escapeCsv(affectation.getEleve().getPrenom())).append(",")
               .append(session != null ? session.getId() : "").append(",")
               .append(session != null ? escapeCsv(session.getActivite().getTitre()) : "").append(",")
               .append(session != null ? escapeCsv(session.getSalle().getNom()) : "").append(",")
               .append(session != null ? escapeCsv(session.getCreneau().getLibelle()) : "")
               .append("\n");
        }
        
        log.info("✅ {} affectations exportées", affectations.size());
        return csv.toString();
    }
    
    /**
     * Échappe les caractères spéciaux CSV (guillemets, virgules, retours à la ligne)
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Si contient des guillemets, virgules ou retours à la ligne, entourer de guillemets
        if (value.contains("\"") || value.contains(",") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
