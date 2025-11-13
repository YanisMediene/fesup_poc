package com.fesup.service;

import com.fesup.entity.*;
import com.fesup.enums.DemiJournee;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfGenerationService {
    
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADER = 12;
    private static final float FONT_SIZE_BODY = 10;
    
    /**
     * Génère le PDF pour un élève donné
     * @return byte[] contenant le PDF
     */
    public byte[] genererTicketEleve(Eleve eleve, List<Affectation> affectations) throws IOException {
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;
                
                // En-tête
                yPosition = drawHeader(content, eleve, yPosition);
                
                // Tableau des affectations
                yPosition = drawAffectationsTable(content, affectations, yPosition);
                
                // Pied de page
                drawFooter(content, page);
            }
            
            // Conversion en byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
    
    private float drawHeader(PDPageContentStream content, Eleve eleve, float yPosition) throws IOException {
        
        PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        // Titre
        content.beginText();
        content.setFont(fontBold, FONT_SIZE_TITLE);
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Planning des Conferences FESUP 2025");
        content.endText();
        yPosition -= 30;
        
        // Infos élève
        content.beginText();
        content.setFont(fontRegular, FONT_SIZE_HEADER);
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText(String.format("Eleve : %s %s", eleve.getPrenom(), eleve.getNom()));
        content.endText();
        yPosition -= 20;
        
        content.beginText();
        content.setFont(fontRegular, FONT_SIZE_HEADER);
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText(String.format("Lycee : %s", eleve.getLycee().getNom()));
        content.endText();
        yPosition -= 40;
        
        return yPosition;
    }
    
    private float drawAffectationsTable(PDPageContentStream content, List<Affectation> affectations, float yPosition) throws IOException {
        
        PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        // Trier par créneau
        List<Affectation> sorted = affectations.stream()
            .sorted((a1, a2) -> {
                Session s1 = a1.getAssignedSession();
                Session s2 = a2.getAssignedSession();
                int demiJourneeComp = s1.getCreneau().getDemiJournee().compareTo(s2.getCreneau().getDemiJournee());
                if (demiJourneeComp != 0) return demiJourneeComp;
                return s1.getCreneau().getHeureDebut().compareTo(s2.getCreneau().getHeureDebut());
            })
            .toList();
        
        // En-têtes du tableau
        float tableTop = yPosition;
        float tableWidth = 495;
        float[] columnWidths = {120, 250, 125}; // Créneau | Activité | Salle
        float rowHeight = 25;
        
        // Ligne d'en-tête
        content.setNonStrokingColor(0.9f, 0.9f, 0.9f); // Gris clair
        content.addRect(MARGIN, yPosition - rowHeight, tableWidth, rowHeight);
        content.fill();
        
        content.setNonStrokingColor(0f, 0f, 0f); // Noir
        content.beginText();
        content.setFont(fontBold, FONT_SIZE_BODY);
        content.newLineAtOffset(MARGIN + 5, yPosition - 17);
        content.showText("Creneau");
        content.endText();
        
        content.beginText();
        content.setFont(fontBold, FONT_SIZE_BODY);
        content.newLineAtOffset(MARGIN + columnWidths[0] + 5, yPosition - 17);
        content.showText("Activite");
        content.endText();
        
        content.beginText();
        content.setFont(fontBold, FONT_SIZE_BODY);
        content.newLineAtOffset(MARGIN + columnWidths[0] + columnWidths[1] + 5, yPosition - 17);
        content.showText("Salle");
        content.endText();
        
        yPosition -= rowHeight;
        
        // Lignes de données
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH'h'mm");
        
        for (int i = 0; i < sorted.size(); i++) {
            Affectation aff = sorted.get(i);
            Session session = aff.getAssignedSession();
            Creneau creneau = session.getCreneau();
            
            // Ligne alternée
            if (i % 2 == 0) {
                content.setNonStrokingColor(0.97f, 0.97f, 0.97f);
                content.addRect(MARGIN, yPosition - rowHeight, tableWidth, rowHeight);
                content.fill();
                content.setNonStrokingColor(0f, 0f, 0f);
            }
            
            // Créneau
            String creneauText = String.format("%s - %s",
                creneau.getHeureDebut().format(timeFormatter),
                creneau.getHeureFin().format(timeFormatter)
            );
            String demiJourneeText = creneau.getDemiJournee().getLabel();
            
            content.beginText();
            content.setFont(fontRegular, FONT_SIZE_BODY);
            content.newLineAtOffset(MARGIN + 5, yPosition - 12);
            content.showText(creneauText);
            content.endText();
            
            content.beginText();
            content.setFont(fontRegular, 8);
            content.newLineAtOffset(MARGIN + 5, yPosition - 20);
            content.showText("(" + demiJourneeText + ")");
            content.endText();
            
            // Activité (avec troncature si trop long)
            String activite = session.getActivite().getTitre();
            if (activite.length() > 35) {
                activite = activite.substring(0, 32) + "...";
            }
            
            content.beginText();
            content.setFont(fontRegular, FONT_SIZE_BODY);
            content.newLineAtOffset(MARGIN + columnWidths[0] + 5, yPosition - 15);
            content.showText(activite);
            content.endText();
            
            // Salle
            content.beginText();
            content.setFont(fontRegular, FONT_SIZE_BODY);
            content.newLineAtOffset(MARGIN + columnWidths[0] + columnWidths[1] + 5, yPosition - 15);
            content.showText(session.getSalle().getNom());
            content.endText();
            
            yPosition -= rowHeight;
        }
        
        // Bordures du tableau
        content.setLineWidth(1f);
        content.moveTo(MARGIN, tableTop);
        content.lineTo(MARGIN, yPosition + rowHeight);
        content.moveTo(MARGIN + tableWidth, tableTop);
        content.lineTo(MARGIN + tableWidth, yPosition + rowHeight);
        content.stroke();
        
        return yPosition - 20;
    }
    
    private void drawFooter(PDPageContentStream content, PDPage page) throws IOException {
        
        PDFont fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        float footerY = 50;
        
        content.beginText();
        content.setFont(fontItalic, 8);
        content.newLineAtOffset(MARGIN, footerY);
        content.showText("Document genere le " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        content.endText();
        
        content.beginText();
        content.setFont(fontItalic, 8);
        content.newLineAtOffset(page.getMediaBox().getWidth() - MARGIN - 150, footerY);
        content.showText("FESUP - Forum des Etudes Superieures");
        content.endText();
    }
}
