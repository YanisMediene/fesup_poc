package com.fesup.enums;

/**
 * Enum représentant une demi-journée sur plusieurs jours
 * Format: JOUR{n}_{PERIODE}
 */
public enum DemiJournee {
    // Jour 1
    JOUR1_MATIN("Jour 1 - Matin", 1, "MATIN"),
    JOUR1_APRES_MIDI("Jour 1 - Après-midi", 1, "APRES_MIDI"),
    
    // Jour 2
    JOUR2_MATIN("Jour 2 - Matin", 2, "MATIN"),
    JOUR2_APRES_MIDI("Jour 2 - Après-midi", 2, "APRES_MIDI");
    
    private final String label;
    private final int numeroJour;
    private final String periode;
    
    DemiJournee(String label, int numeroJour, String periode) {
        this.label = label;
        this.numeroJour = numeroJour;
        this.periode = periode;
    }
    
    public String getLabel() {
        return label;
    }
    
    public int getNumeroJour() {
        return numeroJour;
    }
    
    public String getPeriode() {
        return periode;
    }
    
    /**
     * Vérifie si deux demi-journées sont sur le même jour
     */
    public boolean estMemeJour(DemiJournee autre) {
        return this.numeroJour == autre.numeroJour;
    }
    
    /**
     * Vérifie si deux demi-journées sont à la même période (matin/après-midi)
     */
    public boolean estMemePeriode(DemiJournee autre) {
        return this.periode.equals(autre.periode);
    }
    
    @Override
    public String toString() {
        return label;
    }
}
