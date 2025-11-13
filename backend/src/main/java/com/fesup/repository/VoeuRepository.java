package com.fesup.repository;

import com.fesup.entity.Eleve;
import com.fesup.entity.Voeu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoeuRepository extends JpaRepository<Voeu, Long> {
    // Méthodes CRUD de base fournies par JpaRepository
    
    /**
     * Récupère les vœux d'un élève triés par priorité
     */
    List<Voeu> findByEleveOrderByPrioriteAsc(Eleve eleve);
    
    /**
     * Récupère les vœux d'un élève par son ID
     */
    List<Voeu> findByEleveId(Long eleveId);
    
    /**
     * Compte le nombre de vœux d'un élève
     */
    long countByEleveId(Long eleveId);
    
    /**
     * Charge tous les vœux avec élève et activité (EAGER LOADING pour Timefold)
     */
    @Query("SELECT DISTINCT v FROM Voeu v " +
           "LEFT JOIN FETCH v.eleve e " +
           "LEFT JOIN FETCH e.lycee " +
           "LEFT JOIN FETCH v.activite")
    List<Voeu> findAllWithDetails();
}
