package com.fesup.repository;

import com.fesup.entity.Activite;
import com.fesup.enums.DemiJournee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    
    /**
     * Trouve toutes les activités d'une demi-journée donnée
     */
    List<Activite> findByDemiJournee(DemiJournee demiJournee);
}
