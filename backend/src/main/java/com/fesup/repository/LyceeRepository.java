package com.fesup.repository;

import com.fesup.entity.Lycee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LyceeRepository extends JpaRepository<Lycee, Long> {
    Optional<Lycee> findByNom(String nom);
}
