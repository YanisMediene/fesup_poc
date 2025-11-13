package com.fesup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TicketStorageService {
    
    @Value("${application.tickets.storage-path:/var/fesup/tickets}")
    private String storagePath;
    
    /**
     * Sauvegarde un PDF sur le file system
     * @return Le chemin relatif du fichier sauvegardé
     */
    public String sauvegarderPdf(Long eleveId, byte[] pdfData) throws IOException {
        // Créer l'arborescence : tickets/2025/eleve_123.pdf
        int annee = LocalDateTime.now().getYear();
        Path dossierAnnee = Paths.get(storagePath, String.valueOf(annee));
        
        if (!Files.exists(dossierAnnee)) {
            Files.createDirectories(dossierAnnee);
            log.info("Dossier créé : {}", dossierAnnee);
        }
        
        String nomFichier = String.format("eleve_%d.pdf", eleveId);
        Path cheminComplet = dossierAnnee.resolve(nomFichier);
        
        Files.write(cheminComplet, pdfData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        log.info("PDF sauvegardé : {} ({} bytes)", cheminComplet, pdfData.length);
        
        // Retourner le chemin relatif
        return String.format("%d/%s", annee, nomFichier);
    }
    
    /**
     * Récupère un PDF depuis le file system
     */
    public byte[] recupererPdf(String cheminRelatif) throws IOException {
        Path cheminComplet = Paths.get(storagePath, cheminRelatif);
        
        if (!Files.exists(cheminComplet)) {
            throw new FileNotFoundException("Ticket introuvable : " + cheminRelatif);
        }
        
        return Files.readAllBytes(cheminComplet);
    }
    
    /**
     * Supprime un PDF
     */
    public void supprimerPdf(String cheminRelatif) throws IOException {
        Path cheminComplet = Paths.get(storagePath, cheminRelatif);
        Files.deleteIfExists(cheminComplet);
        log.info("PDF supprimé : {}", cheminComplet);
    }
    
    /**
     * Supprime tous les PDFs du répertoire de stockage
     */
    public int supprimerTousPdfs() throws IOException {
        Path storageDirPath = Paths.get(storagePath);
        
        if (!Files.exists(storageDirPath)) {
            log.info("Le répertoire de tickets n'existe pas : {}", storagePath);
            return 0;
        }
        
        int count = 0;
        try (var stream = Files.walk(storageDirPath)) {
            var pdfFiles = stream.filter(Files::isRegularFile)
                                  .filter(path -> path.toString().endsWith(".pdf"))
                                  .toList();
            
            for (Path pdfFile : pdfFiles) {
                Files.delete(pdfFile);
                count++;
            }
        }
        
        log.info("🗑️  {} fichiers PDF supprimés du répertoire {}", count, storagePath);
        return count;
    }
}
