package com.fesup.repository;

import com.fesup.entity.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EleveRepository extends JpaRepository<Eleve, Long> {
    
    /**
     * Trouve un élève par son ID National et son nom (en majuscules)
     * Utilisé pour l'authentification
     */
    Optional<Eleve> findByIdNationalAndNom(String idNational, String nom);
    
    /**
     * Trouve un élève par son ID National uniquement
     */
    Optional<Eleve> findByIdNational(String idNational);
    
    /**
     * Compte le nombre d'élèves ayant soumis leurs vœux
     */
    long countByVoeuxSoumisTrue();
    
    /**
     * Charge tous les élèves avec leur lycée (EAGER LOADING pour Timefold)
     */
    @Query("SELECT DISTINCT e FROM Eleve e LEFT JOIN FETCH e.lycee")
    List<Eleve> findAllWithLycee();
}
