package com.fesup.repository;

import com.fesup.entity.Creneau;
import com.fesup.enums.DemiJournee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreneauRepository extends JpaRepository<Creneau, Long> {
    List<Creneau> findByDemiJournee(DemiJournee demiJournee);
}
