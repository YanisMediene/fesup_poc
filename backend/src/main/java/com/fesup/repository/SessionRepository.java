package com.fesup.repository;

import com.fesup.entity.Session;
import com.fesup.enums.DemiJournee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    /**
     * Trouve toutes les sessions pour une demi-journée donnée
     */
    @Query("SELECT s FROM Session s WHERE s.creneau.demiJournee = :demiJournee")
    List<Session> findByDemiJournee(@Param("demiJournee") DemiJournee demiJournee);
    
    /**
     * Trouve toutes les sessions d'une activité
     */
    List<Session> findByActiviteId(Long activiteId);
    
    /**
     * Compte le nombre de sessions d'une activité dans une demi-journée
     */
    @Query("SELECT COUNT(s) FROM Session s WHERE s.activite.id = :activiteId AND s.creneau.demiJournee = :demiJournee")
    long countByActiviteIdAndDemiJournee(@Param("activiteId") Long activiteId, @Param("demiJournee") DemiJournee demiJournee);
    
    /**
     * Trouve toutes les sessions utilisant un créneau donné
     */
    List<Session> findByCreneauId(Long creneauId);
    
    /**
     * Charge toutes les sessions avec activité, salle et créneau (EAGER LOADING pour Timefold)
     */
    @Query("SELECT DISTINCT s FROM Session s " +
           "LEFT JOIN FETCH s.activite " +
           "LEFT JOIN FETCH s.salle " +
           "LEFT JOIN FETCH s.creneau")
    List<Session> findAllWithDetails();
}
