package com.fesup.repository;

import com.fesup.entity.Affectation;
import com.fesup.entity.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    
    List<Affectation> findByEleveId(Long eleveId);
    
    @Query("SELECT DISTINCT a FROM Affectation a " +
           "LEFT JOIN FETCH a.eleve e " +
           "LEFT JOIN FETCH e.lycee " +
           "LEFT JOIN FETCH a.assignedSession s " +
           "LEFT JOIN FETCH s.activite " +
           "LEFT JOIN FETCH s.salle " +
           "LEFT JOIN FETCH s.creneau c " +
           "WHERE a.assignedSession IS NOT NULL " +
           "ORDER BY e.nom ASC, e.prenom ASC, c.heureDebut ASC")
    List<Affectation> findAllWithSession();
    
    List<Affectation> findByEleveAndAssignedSessionIsNotNull(Eleve eleve);
    
    void deleteByEleveId(Long eleveId);
}
